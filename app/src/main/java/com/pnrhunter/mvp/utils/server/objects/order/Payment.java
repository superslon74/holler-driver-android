package com.pnrhunter.mvp.utils.server.objects.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Payment implements Parcelable {
    @Expose(deserialize = false)
    @SerializedName("fixed")
    public String fixed;
    @Expose(deserialize = false)
    @SerializedName("distance")
    public String distance;
    @Expose(deserialize = false)
    @SerializedName("discount")
    public String discount;
    @Expose(deserialize = false)
    @SerializedName("wallet")
    public String wallet;
    @Expose(deserialize = false)
    @SerializedName("total")
    public String total;
    @Expose(deserialize = false)
    @SerializedName("payable")
    public String payable;

    public Payment(){

    }

    protected Payment(Parcel in) {
        fixed = in.readString();
        distance = in.readString();
        discount = in.readString();
        wallet = in.readString();
        total = in.readString();
        payable = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fixed);
        dest.writeString(distance);
        dest.writeString(discount);
        dest.writeString(wallet);
        dest.writeString(total);
        dest.writeString(payable);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Payment> CREATOR = new Creator<Payment>() {
        @Override
        public Payment createFromParcel(Parcel in) {
            return new Payment(in);
        }

        @Override
        public Payment[] newArray(int size) {
            return new Payment[size];
        }
    };
}