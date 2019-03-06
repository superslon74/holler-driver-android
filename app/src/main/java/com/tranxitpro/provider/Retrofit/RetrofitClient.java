package com.tranxitpro.provider.Retrofit;

import com.tranxitpro.provider.Helper.URLHelper;
import com.tranxitpro.provider.Models.AccessDetails;

import retrofit2.Retrofit;

/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static Retrofit retrofitAcceptReject = null;

    public static Retrofit getLiveTrackingClient() {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(AccessDetails.serviceurl)
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getAcceptRejectClient() {
        if (retrofitAcceptReject==null) {
            retrofitAcceptReject = new Retrofit.Builder()
                    .baseUrl(AccessDetails.serviceurl)
                    .build();
        }
        return retrofitAcceptReject;
    }
}
