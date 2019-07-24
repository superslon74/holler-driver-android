package com.pnrhunter.mvp.utils.server.objects.order;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pnrhunter.mvp.utils.server.ServerAPI;

import java.util.List;

public class CheckStatusResponse {
    @Expose(deserialize = false)
    @SerializedName("account_status")
    public String accountStatus;
    @Expose(deserialize = false)
    @SerializedName("service_status")
    public String serviceStatus;
    @Expose(deserialize = false)
    @SerializedName("requests")
    public List<RequestedOrderResponse> requests;

    //TODO: move it to another request
    @Expose(deserialize = false)
    @SerializedName("searching")
    public List<OrderResponse> requestsInSearching;
}
