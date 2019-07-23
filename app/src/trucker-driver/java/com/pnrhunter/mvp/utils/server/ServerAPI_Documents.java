package com.pnrhunter.mvp.utils.server;

import com.pnrhunter.mvp.utils.server.objects.Document;

import java.util.List;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServerAPI_Documents {

    @GET("api/provider/documents")
    Single<List<Document>> getRequiredDocuments(
            @Header(ServerAPI.HEADER_KEY_AUTHORIZATION) String header,
            @Query("device_type") String devicetype,
            @Query("device_id") String deviceId,
            @Query("device_token") String deviceToken
    );

    @Multipart
    @POST("api/provider/documents/{id}")
    Single<Document> sendDocument(
            @Header(ServerAPI.HEADER_KEY_AUTHORIZATION) String header,
            @Part("device_type") RequestBody devicetype,
            @Part("device_id") RequestBody deviceId,
            @Part("device_token") RequestBody deviceToken,

            @Path("id") String documentId,

            @Part MultipartBody.Part document
    );
}
