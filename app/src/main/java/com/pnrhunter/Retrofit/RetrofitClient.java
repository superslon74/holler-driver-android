package com.pnrhunter.Retrofit;

import com.pnrhunter.Models.AccessDetails;

import retrofit2.Retrofit;

/*
* STRANGE CODE?
* Creates two retrofit instances with same url
* */

@Deprecated
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
