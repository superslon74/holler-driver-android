package com.holler.app.di;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.holler.app.Activity.DocumentsActivity;
import com.holler.app.Activity.RegisterActivity;
import com.holler.app.Helper.URLHelper;
import com.holler.app.Services.UserStatusChecker;
import com.holler.app.server.OrderServerApi;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

@Module
public class RetrofitModule {

    @Provides
    @Singleton
    public ServerAPI provideRetrofitServerAPI(){

        ConnectionPool pool = new ConnectionPool(10, 10000, TimeUnit.MILLISECONDS);

        OkHttpClient httpClient = new OkHttpClient
                .Builder()
                .cache(null)
                .followRedirects(true)
                .followSslRedirects(true)
                .connectionPool(pool)
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

//    TODO: extend this interface with other server interfaces
    public interface ServerAPI {
        String HEADER_KEY_AUTHORIZATION = "Authorization";

        @POST("api/provider/verify")
        Call<JsonObject> checkEmailExists(@HeaderMap Map<String, String> headers, @Body User user);

        @POST("api/provider/register")
        Call<JsonObject> register(@HeaderMap Map<String, String> headers, @Body User user);

        @POST("api/provider/oauth/token")
        Call<JsonObject> signIn(@HeaderMap Map<String, String> headers, @Body User user);

        @GET("api/provider/profile")
        Call<User> profile(@HeaderMap Map<String, String> headers);


    }
}
