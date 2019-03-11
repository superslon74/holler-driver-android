package com.andar.hand.Retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("/api/provider/trip/{id}/calculate")
    Call<ResponseBody> getLiveTracking(@Header("X-Requested-With") String xmlRequest, @Header("Authorization") String strToken,
                                       @Path("id") String id,
                                       @Field("latitude") String latitude, @Field("longitude") String longitude);


    @POST("/api/provider/trip/{id}")
    Call<ResponseBody> acceptAPI(@Path("id") String id, @Header("X-Requested-With") String xmlRequest,
                                 @Header("Authorization") String accesskey);

    @DELETE("/api/provider/trip/{id}")
    Call<ResponseBody> rejectAPI(@Path("id") String id, @Header("X-Requested-With") String xmlRequest,
                                 @Header("Authorization") String accesskey);
}
