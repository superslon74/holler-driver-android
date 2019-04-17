package com.holler.app.di;

import com.google.gson.JsonObject;
import com.holler.app.Helper.URLHelper;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

@Module
public class RetrofitModule {
    private static JsonObject STATUS_OFFLINE_JSON;
    private static JsonObject STATUS_ONLINE_JSON;
    static {
        STATUS_OFFLINE_JSON = new JsonObject();
        STATUS_OFFLINE_JSON.addProperty("service_status", "offline");

        STATUS_ONLINE_JSON = new JsonObject();
        STATUS_ONLINE_JSON.addProperty("service_status", "active");
    }


    @Provides
    @Singleton
    public ServerAPI provideRetrofitServerAPI() {

        ConnectionPool pool = new ConnectionPool(10, 20000, TimeUnit.MILLISECONDS);

        OkHttpClient httpClient = new OkHttpClient
                .Builder()
                .cache(null)
                .followRedirects(true)
                .followSslRedirects(true)
                .connectionPool(pool)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        return chain.proceed(chain
                                .request()
                                .newBuilder()
                                .addHeader("X-Requested-With", "XMLHttpRequest")
                                .build());
                    }
                })
                .build();

        ServerAPI retrofitClient = new Retrofit
                .Builder()
                .client(httpClient)
                .baseUrl(URLHelper.base)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ServerAPI.class);

        return retrofitClient;
    }

    public interface ServerAPI {
        String HEADER_KEY_AUTHORIZATION = "Authorization";
        JsonObject STATUS_OFFLINE = STATUS_OFFLINE_JSON;
        JsonObject STATUS_ONLINE = STATUS_ONLINE_JSON;


        @POST("api/provider/verify")
        Call<JsonObject> checkEmailExists(
                @HeaderMap Map<String, String> headers,
                @Body User user
        );

        @POST("api/provider/register")
        Call<JsonObject> register(
                @HeaderMap Map<String, String> headers,
                @Body User user
        );

        @POST("api/provider/oauth/token")
        Call<JsonObject> signIn(
                @Body User user
        );

        @GET("api/provider/profile")
        Call<User> profile(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader
        );

        @GET("api/provider/trip")
        Call<JsonObject> sendTripLocation(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
                @Query("latitude") String latitude,
                @Query("longitude") String longitude
        );

        @FormUrlEncoded
        @POST("/api/provider/trip/{id}/calculate")
        Call<ResponseBody> getLiveTracking(
                @Header("Authorization") String authHeader,
                @Path("id") String tripId,
                @Field("latitude") String latitude,
                @Field("longitude") String longitude
        );

        @POST("api/provider/profile/available")
        Call<JsonObject> sendStatus(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
                @Body JsonObject status
        );

    }
}
