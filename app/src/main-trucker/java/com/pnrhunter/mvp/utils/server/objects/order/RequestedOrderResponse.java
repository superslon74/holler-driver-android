package com.pnrhunter.mvp.utils.server.objects.order;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pnrhunter.mvp.utils.server.ServerAPI;

public class RequestedOrderResponse {
    @Expose(deserialize = false)
    @SerializedName("id")
    public String id;
    @Expose(deserialize = false)
    @SerializedName("request_id")
    public String requestId;
    @Expose(deserialize = false)
    @SerializedName("provider_id")
    public String providerId;
    @Expose(deserialize = false)
    @SerializedName("status")
    @Deprecated
    public String status;
    @Expose(deserialize = false)
    @SerializedName("time_left_to_respond")
    public int timeToRespond;

    @Expose(deserialize = false)
    @SerializedName("request")
    public OrderResponse order;

    @Override
    public boolean equals(@Nullable Object obj) {
        try{
            return ((RequestedOrderResponse) obj).id.equals(this.id);
        }catch (ClassCastException | NullPointerException e){
            return false;
        }
    }
}
