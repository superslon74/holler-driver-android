package com.holler.app.di.app.modules;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.holler.app.Helper.URLHelper;
import com.holler.app.activity.DocumentsActivity;
import com.holler.app.di.User;
import com.holler.app.mvp.main.OrderModel;
import com.holler.app.server.OrderServerApi;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import androidx.annotation.Nullable;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ConnectionPool;
import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

@Module
public class RetrofitModule {
    private static final String LOG_TAG = "RETROFIT";
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

        ConnectionPool pool = new ConnectionPool(20, 20000, TimeUnit.MILLISECONDS);

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

        RxJavaPlugins.setErrorHandler(throwable -> {
            Logger.wtf("Rx plugin error handler",throwable);
            Crashlytics.log(Log.ERROR, LOG_TAG, throwable.getMessage());
            Crashlytics.logException(throwable);

            throwable.printStackTrace();
        });

        ServerAPI retrofitClient = new Retrofit
                .Builder()
                .client(httpClient)
                .baseUrl(ServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(rxAdapter)
                .build()
                .create(ServerAPI.class);

        return retrofitClient;
    }

    public interface ServerAPI {
        String BASE_URL = "https://api.holler.taxi/";
        String HEADER_KEY_AUTHORIZATION = "Authorization";
        JsonObject STATUS_OFFLINE = STATUS_OFFLINE_JSON;
        JsonObject STATUS_ONLINE = STATUS_ONLINE_JSON;

        //      auth
        @POST("api/provider/oauth/token")
        Single<AccessTokenResponseBody> getAccessToken(
                @Body AccessTokenRequestBody user
        );


        class AccessTokenRequestBody {
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

        class AccessTokenResponseBody {
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

        class LogoutRequestBody {
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

        class ForgotPasswordRequestBody {
            @Expose(deserialize = false)
            @SerializedName("email")
            public String email;

            public ForgotPasswordRequestBody(String email) {
                this.email = email;
            }
        }

        class ForgotPasswordResponseBody {
            @Expose(serialize = false)
            @SerializedName("provider")
            public Provider provider;

            //TODO: remove otp checking to server
            class Provider {
                @Expose(serialize = false)
                @SerializedName("id")
                public String id;
                @Expose(serialize = false)
                @SerializedName("otp")
                public String otp;
            }

            public String getId() {
                if (provider != null)
                    return provider.id;
                return null;
            }

            public String getOtp() {
                if (provider != null)
                    return provider.otp;
                return null;
            }
        }

        @POST("api/provider/reset/password")
        Single<JsonObject> changePassword(
                @Body ChangePasswordRequestBody newPassword
        );

        class ChangePasswordRequestBody {
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

        class CheckStatusResponse {
            @Expose(deserialize = false)
            @SerializedName("account_status")
            public String accountStatus;
            @Expose(deserialize = false)
            @SerializedName("service_status")
            public String serviceStatus;
            @Expose(deserialize = false)
            @SerializedName("requests")
            public List<RequestedOrderResponse> requests;
        }
        class RequestedOrderResponse {
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
        class OrderResponse {
            @Expose(deserialize = false)
            @SerializedName("id")
            public String id;
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
            public JsonElement user;
            @Expose(deserialize = false)
            @SerializedName("payment")
            public String payment;


        }

        @POST("api/provider/profile/available")
        Single<JsonObject> sendStatus(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
                @Body JsonObject status
        );


        @POST("api/provider/verify")
        Single<JsonObject> checkEmailExists(
                @Body EmailVerificationRequestBody email
        );

        class EmailVerificationRequestBody {
            @Expose(deserialize = false)
            @SerializedName("email")
            public String email;

            public EmailVerificationRequestBody(String email) {
                this.email = email;
            }
        }

        @POST("api/provider/register")
        Single<JsonObject> register(
                @Body User user
        );

//        ORDER

        @POST("api/provider/trip/send_request")
        Single<CreateOrderResponse> createOrder(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
                @Body CreateOrderRequestBody body
        );

        class CreateOrderRequestBody {
            @Expose(deserialize = false)
            @SerializedName("s_latitude")
            public String startLatitude;
            @Expose(deserialize = false)
            @SerializedName("s_longitude")
            public String startLongitude;

            public CreateOrderRequestBody(String startLatitude, String startLongitude) {
                this.startLatitude = startLatitude;
                this.startLongitude = startLongitude;
            }
        }
        class CreateOrderResponse{
            @Expose(serialize = false)
            @SerializedName("message")
            public String message;
            @Expose(serialize = false)
            @SerializedName("request_id")
            public String requestId;
            @Expose(serialize = false)
            @SerializedName("current_provider")
            public String provider;

            private static final String MESSAGE_REQUEST_SUCCESSFULL = "New request Created!";
            public boolean isSuccessfullyCreated(){
                return MESSAGE_REQUEST_SUCCESSFULL.equals(message);
            }
        }

        String ORDER_DETAILS_TYPE_PAST = "history";
        String ORDER_DETAILS_TYPE_UPCOMING = "upcoming";
        @GET("api/provider/requests/{type}/details")
        Single<List<OrderResponse>> getOrderDetails(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
                @Path("type") String type,
                @Query("request_id") String id
        );

        @POST("api/provider/trip/{id}")
        Single<JsonElement> acceptOrder(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
                @Path("id") String id);

        @DELETE("api/provider/trip/{id}")
        Single<JsonElement> rejectOrder(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
                @Path("id") String orderId);

        @POST("api/provider/trip/{id}/rate")
        Single<JsonObject> rateOrder(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
                @Path("id") String id,
                @Body UpdateOrderRequestBody body);

        @POST("api/provider/trip/{id}")
        Single<JsonObject> updateOrder(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
                @Path("id") String id,
                @Body UpdateOrderRequestBody body);

        class UpdateOrderRequestBody{
            @Expose(deserialize = false)
            @SerializedName("_method")
            public String method;
            @Expose(deserialize = false)
            @SerializedName("status")
            public String status;
            @Expose(deserialize = false)
            @SerializedName("rating")
            public String rating;
            @Expose(deserialize = false)
            @SerializedName("comment")
            public String comment;
            @Expose(deserialize = false)
            @SerializedName("address")
            public String address;

            public UpdateOrderRequestBody(String method,
                                          String status,
                                          String rating,
                                          String comment,
                                          String address) {
                this.method = method;
                this.status = status;
                this.rating = rating;
                this.comment = comment;
                this.address = address;
            }
        }

        @POST("api/provider/cancel")
        Single<JsonObject> cancelOrder(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
                @Body CancelOrderRequestBody body);

        class CancelOrderRequestBody{
            @Expose(deserialize = false)
            @SerializedName("id")
            public String id;

            public CancelOrderRequestBody(String id) {
                this.id = id;
            }
        }

        //profile
        @Multipart
        @POST("api/provider/profile")
        Single<JsonObject> updateProfile(
                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
                @Part("first_name") String fn,
                @Part("last_name") String ln,
                @Part("email") String e,
                @Part("mobile") String m,
                @Part("gender") String g,
                @Part MultipartBody.Part avatar
        );

//        documents

        @GET("api/provider/documents")
        Single<List<Document>> getRequiredDocuments(
                @Header(HEADER_KEY_AUTHORIZATION) String header,
                @Query("device_type") String devicetype,
                @Query("device_id") String deviceId,
                @Query("device_token") String deviceToken
        );

        class Document implements Parcelable {
            @Expose(serialize = false)
            @SerializedName("id")
            public String id;
            @Expose(serialize = false)
            @SerializedName("name")
            public String name;
            @Expose(serialize = false)
            @SerializedName("url")
            public String remoteUrl;

            public String localUrl;

            public Document() {
            }

            public Document(String id, String name, String remoteUrl, String localUrl) {
                this.id = id;
                this.name = name;
                this.remoteUrl = remoteUrl;
                this.localUrl = localUrl;
            }

            protected Document(Parcel in) {
                id = in.readString();
                name = in.readString();
                remoteUrl = in.readString();
                localUrl = in.readString();
            }

            public static final Creator<Document> CREATOR = new Creator<Document>() {
                @Override
                public Document createFromParcel(Parcel in) {
                    return new Document(in);
                }

                @Override
                public Document[] newArray(int size) {
                    return new Document[size];
                }
            };

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(id);
                dest.writeString(name);
                dest.writeString(remoteUrl);
                dest.writeString(localUrl);
            }

            public boolean isRequired() {
                return this.remoteUrl==null || "".equals(this.remoteUrl);
            }
        }

        @Multipart
        @POST("api/provider/documents/{id}")
        Single<Document> sendDocument(
                @Header(HEADER_KEY_AUTHORIZATION) String header,
                @Part("device_type") RequestBody devicetype,
                @Part("device_id") RequestBody deviceId,
                @Part("device_token") RequestBody deviceToken,

                @Path("id") String documentId,

                @Part MultipartBody.Part document
        );

        //trash


        @POST("api/provider/oauth/token")
        Call<JsonObject> signIn(
                @Body User user
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
