package com.holler.app.di.app.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.holler.app.Helper.URLHelper;
import com.holler.app.di.User;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ConnectionPool;
import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
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

                        Request request = chain
                                .request()
                                .newBuilder()
                                .addHeader("X-Requested-With", "XMLHttpRequest")
                                .build();
//                        try {
                            return chain.proceed(request);
//                        }catch (Exception e){
//                            Logger.e(e.getMessage());
//                            return chain
//                                    .withWriteTimeout(10,TimeUnit.SECONDS)
//                                    .proceed(request);
//                        }
                    }
                })
                .build();

        Scheduler scheduler = Schedulers.newThread();

        RxJava2CallAdapterFactory rxAdapter =
                RxJava2CallAdapterFactory
                        .createWithScheduler(scheduler);

        ServerAPI retrofitClient = new Retrofit
                .Builder()
                .client(httpClient)
                .baseUrl(URLHelper.base)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(rxAdapter)
                .build()
                .create(ServerAPI.class);

        return retrofitClient;
    }

    public interface ServerAPI {
        String HEADER_KEY_AUTHORIZATION = "Authorization";
        JsonObject STATUS_OFFLINE = STATUS_OFFLINE_JSON;
        JsonObject STATUS_ONLINE = STATUS_ONLINE_JSON;

        //      auth
        @POST("api/provider/oauth/token")
        Single<AccessTokenResponseBody> getAccessToken(
                @Body AccessTokenRequestBody user
        );
        class AccessTokenRequestBody{
            @Expose
            @SerializedName("email")
            public String email;
            @Expose
            @SerializedName("password")
            public String password;

            @Expose
            @SerializedName("device_type")
            public String deviceType;
            @Expose
            @SerializedName("device_id")
            public String deviceId;
            @Expose
            @SerializedName("device_token")
            public String deviceToken;

            public AccessTokenRequestBody(String email, String password, String deviceType, String deviceId, String deviceToken) {
                this.email = email;
                this.password = password;
                this.deviceType = deviceType;
                this.deviceId = deviceId;
                this.deviceToken = deviceToken;
            }
        }
        class AccessTokenResponseBody{
            @Expose
            @SerializedName("access_token")
            public String token;
        }

        @GET("api/provider/profile")
        Single<User> getUserProfile(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader
        );

        @POST("api/provider/logout")
        Single<JsonObject> logout(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
                @Body LogoutRequestBody user
        );
        class LogoutRequestBody{
            @Expose(deserialize = false)
            @SerializedName("id")
            public String userId;

            public LogoutRequestBody(String userId) {
                this.userId = userId;
            }
        }

        // change password
        @POST("api/provider/forgot/password")
        Single<ForgotPasswordResponseBody> forgotPassword(
                @Body ForgotPasswordRequestBody email
        );

        class ForgotPasswordRequestBody{
            @Expose(deserialize = false)
            @SerializedName("email")
            public String email;

            public ForgotPasswordRequestBody(String email) {
                this.email = email;
            }
        }
        class ForgotPasswordResponseBody{
            @Expose(serialize = false)
            @SerializedName("provider")
            public Provider provider;
            //TODO: remove otp checking to server
            class Provider{
                @Expose(serialize = false)
                @SerializedName("id")
                public String id;
                @Expose(serialize = false)
                @SerializedName("otp")
                public String otp;
            }

            public String getId(){
                if(provider!=null)
                    return provider.id;
                return null;
            }

            public String getOtp(){
                if(provider!=null)
                    return provider.otp;
                return null;
            }
        }

        @POST("api/provider/reset/password")
        Single<JsonObject> changePassword(
                @Body ChangePasswordRequestBody newPassword
        );
        class ChangePasswordRequestBody{
            @Expose(deserialize = false)
            @SerializedName("id")
            public String id;
            @Expose(deserialize = false)
            @SerializedName("password")
            public String password;
            @Expose(deserialize = false)
            @SerializedName("password_confirmation")
            public String passwordConfirmation;

            public ChangePasswordRequestBody(String id, String password, String passwordConfirmation) {
                this.id = id;
                this.password = password;
                this.passwordConfirmation = passwordConfirmation;
            }
        }

        //check status
        @GET("api/provider/trip")
        Single<CheckStatusResponse> checkStatus(
            @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
            @Query("latitude") String latitude,
            @Query("longitude") String longitude
        );

        class CheckStatusResponse{
            @Expose(deserialize = false)
            @SerializedName("account_status")
            public String accountStatus;
            @Expose(deserialize = false)
            @SerializedName("service_status")
            public String serviceStatus;
            @Expose(deserialize = false)
            @SerializedName("requests")
            public JsonArray requests;
        }

        @POST("api/provider/profile/available")
        Single<JsonObject> sendStatus(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
                @Body JsonObject status
        );



        @POST("api/provider/verify")
        Call<JsonObject> checkEmailExists(
                @Body User user
        );

        @POST("api/provider/register")
        Call<JsonObject> register(
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




    }
}
