package com.pnrhunter.mvp.utils.server.objects.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pnrhunter.mvp.utils.server.ServerAPI;

public class OrderResponse implements Parcelable {
    @Expose(deserialize = false)
    @SerializedName("id")
    public String id;
    @Expose(deserialize = false)
    @SerializedName("static_map")
    public String mapImage;
    @Expose(deserialize = false)
    @SerializedName("booking_id")
    public String bookingId;
    @Expose(deserialize = false)
    @SerializedName("provider_send")
    public String providerSend;
    @Expose(deserialize = false)
    @SerializedName("user_id")
    public String userId;
    @Expose(deserialize = false)
    @SerializedName("provider_id")
    public String providerId;
    @Expose(deserialize = false)
    @SerializedName("current_provider_id")
    public String currentProviderId;
    @Expose(deserialize = false)
    @SerializedName("service_type_id")
    public String serviceTypeId;
    @Expose(deserialize = false)
    @SerializedName("rental_hours")
    public String rentalHours;
    @Expose(deserialize = false)
    @SerializedName("status")
    public String status;
    @Expose(deserialize = false)
    @SerializedName("cancelled_by")
    public String cancelledBy;
    @Expose(deserialize = false)
    @SerializedName("cancel_reason")
    public String cancelReason;
    @Expose(deserialize = false)
    @SerializedName("is_track")
    public String isTrack;
    @Expose(deserialize = false)
    @SerializedName("paid")
    public String paid;
    @Expose(deserialize = false)
    @SerializedName("payment_mode")
    public String paymentMode;
    @Expose(deserialize = false)
    @SerializedName("distance")
    public String distance;
    @Expose(deserialize = false)
    @SerializedName("travel_time")
    public String travelTime;
    @Expose(deserialize = false)
    @SerializedName("s_address")
    public String sAddress;
    @Expose(deserialize = false)
    @SerializedName("d_address")
    public String dAddress;
    @Expose(deserialize = false)
    @SerializedName("s_latitude")
    public String sLatitude;
    @Expose(deserialize = false)
    @SerializedName("s_longitude")
    public String sLongitude;
    @Expose(deserialize = false)
    @SerializedName("otp")
    public String otp;
    @Expose(deserialize = false)
    @SerializedName("otp_required")
    public String otp_required;
    @Expose(deserialize = false)
    @SerializedName("d_latitude")
    public String dLatitude;
    @Expose(deserialize = false)
    @SerializedName("track_distance")
    public String trackDistance;
    @Expose(deserialize = false)
    @SerializedName("track_latitude")
    public String trackLatitude;
    @Expose(deserialize = false)
    @SerializedName("track_longitude")
    public String trackLongitude;
    @Expose(deserialize = false)
    @SerializedName("d_longitude")
    public String dLongitude;
    @Expose(deserialize = false)
    @SerializedName("assigned_at")
    public String assignedAt;
    @Expose(deserialize = false)
    @SerializedName("schedule_at")
    public String scheduleAt;
    @Expose(deserialize = false)
    @SerializedName("finished_at")
    public String finishedAt;
    @Expose(deserialize = false)
    @SerializedName("started_at")
    public String startedAt;
    @Expose(deserialize = false)
    @SerializedName("user_rated")
    public String userRated;
    @Expose(deserialize = false)
    @SerializedName("provider_rated")
    public String providerRated;
    @Expose(deserialize = false)
    @SerializedName("use_wallet")
    public String useWallet;
    @Expose(deserialize = false)
    @SerializedName("surge")
    public String surge;
    @Expose(deserialize = false)
    @SerializedName("route_key")
    public String routeKey;
    @Expose(deserialize = false)
    @SerializedName("deleted_at")
    public String deletedAt;
    @Expose(deserialize = false)
    @SerializedName("created_at")
    public String createdAt;
    @Expose(deserialize = false)
    @SerializedName("updated_at")
    public String updatedAtw;
    @Expose(deserialize = false)
    @SerializedName("user")
    public OrderResponseUser user;
    @Expose(deserialize = false)
    @SerializedName("payment")
    public Payment payment;
    @Expose(deserialize = false)
    @SerializedName("tax")
    public String tax;
    @Expose(deserialize = false)
    @SerializedName("rating")
    public Rating rating;
    @Expose(serialize = false)
    @SerializedName("service_type")
    public Service service;

    //TODO: server

    public int weight;


    public OrderResponse(){

    }

    protected OrderResponse(Parcel in) {
        id = in.readString();
        mapImage = in.readString();
        bookingId = in.readString();
        providerSend = in.readString();
        userId = in.readString();
        providerId = in.readString();
        currentProviderId = in.readString();
        serviceTypeId = in.readString();
        rentalHours = in.readString();
        status = in.readString();
        cancelledBy = in.readString();
        cancelReason = in.readString();
        isTrack = in.readString();
        paid = in.readString();
        paymentMode = in.readString();
        distance = in.readString();
        travelTime = in.readString();
        sAddress = in.readString();
        dAddress = in.readString();
        sLatitude = in.readString();
        sLongitude = in.readString();
        otp = in.readString();
        otp_required = in.readString();
        dLatitude = in.readString();
        trackDistance = in.readString();
        trackLatitude = in.readString();
        trackLongitude = in.readString();
        dLongitude = in.readString();
        assignedAt = in.readString();
        scheduleAt = in.readString();
        finishedAt = in.readString();
        startedAt = in.readString();
        userRated = in.readString();
        providerRated = in.readString();
        useWallet = in.readString();
        surge = in.readString();
        routeKey = in.readString();
        deletedAt = in.readString();
        createdAt = in.readString();
        updatedAtw = in.readString();
        user = in.readParcelable(OrderResponseUser.class.getClassLoader());
        payment = in.readParcelable(Payment.class.getClassLoader());
        tax = in.readString();
        rating = in.readParcelable(Rating.class.getClassLoader());
        service = in.readParcelable(Service.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(mapImage);
        dest.writeString(bookingId);
        dest.writeString(providerSend);
        dest.writeString(userId);
        dest.writeString(providerId);
        dest.writeString(currentProviderId);
        dest.writeString(serviceTypeId);
        dest.writeString(rentalHours);
        dest.writeString(status);
        dest.writeString(cancelledBy);
        dest.writeString(cancelReason);
        dest.writeString(isTrack);
        dest.writeString(paid);
        dest.writeString(paymentMode);
        dest.writeString(distance);
        dest.writeString(travelTime);
        dest.writeString(sAddress);
        dest.writeString(dAddress);
        dest.writeString(sLatitude);
        dest.writeString(sLongitude);
        dest.writeString(otp);
        dest.writeString(otp_required);
        dest.writeString(dLatitude);
        dest.writeString(trackDistance);
        dest.writeString(trackLatitude);
        dest.writeString(trackLongitude);
        dest.writeString(dLongitude);
        dest.writeString(assignedAt);
        dest.writeString(scheduleAt);
        dest.writeString(finishedAt);
        dest.writeString(startedAt);
        dest.writeString(userRated);
        dest.writeString(providerRated);
        dest.writeString(useWallet);
        dest.writeString(surge);
        dest.writeString(routeKey);
        dest.writeString(deletedAt);
        dest.writeString(createdAt);
        dest.writeString(updatedAtw);
        dest.writeParcelable(user, flags);
        dest.writeParcelable(payment, flags);
        dest.writeString(tax);
        dest.writeParcelable(rating, flags);
        dest.writeParcelable(service, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OrderResponse> CREATOR = new Creator<OrderResponse>() {
        @Override
        public OrderResponse createFromParcel(Parcel in) {
            return new OrderResponse(in);
        }

        @Override
        public OrderResponse[] newArray(int size) {
            return new OrderResponse[size];
        }
    };

    public boolean hasUser() {
        return user!=null;
    }

    public String getFormatedDate() {
        return createdAt;
    }

    public boolean hasStartAddress() {
        return sAddress!=null && !"".equals(sAddress);
    }

    public boolean hasFinishAddress() {
        return dAddress!=null && !"".equals(dAddress);
    }

    public boolean hasComment() {
        return rating!=null && rating.comment!=null && !"".equals(rating.comment);
    }

    public boolean hasPayment() {
        return payment!=null;
    }


    public boolean isScheduled() {
        return scheduleAt!=null && !"".equals(scheduleAt);
    }

    public boolean hasService() {
        return service!=null;
    }

    public boolean hasRating() {
        return user!=null && user.rating != null && user.getRating()>=0;
    }
}
