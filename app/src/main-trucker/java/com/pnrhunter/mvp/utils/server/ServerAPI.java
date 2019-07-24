package com.pnrhunter.mvp.utils.server;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pnrhunter.mvp.utils.server.objects.Document;
import com.pnrhunter.mvp.utils.server.objects.User;
import com.pnrhunter.mvp.utils.server.objects.order.CheckStatusResponse;
import com.pnrhunter.mvp.utils.server.objects.order.OrderResponse;

import java.util.List;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServerAPI {
    String BASE_URL = "https://api.holler.taxi/";
    String HEADER_KEY_AUTHORIZATION = "Authorization";

    //server config

    @GET("api/provider/playStoreLink")
    Single<ApplicationLinkResponse> getApplicationLink();
    class ApplicationLinkResponse{
        @Expose
        @SerializedName("link")
        public String link;
    }

    @GET("api/provider/socialLoginStatus")
    Single<SocialLoginStatusResponse> getSocialLoginStatus();
    class SocialLoginStatusResponse{
        @Expose
        @SerializedName("isEnabled")
        public boolean isEnabled;
    }

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

    @GET("api/provider/requests/upcoming")
    Single<List<OrderResponse>> getUpcomingTrips(
            @Header(HEADER_KEY_AUTHORIZATION) String authHeader);
    @GET("api/provider/requests/history")
    Single<List<OrderResponse>> getPastTrips(
            @Header(HEADER_KEY_AUTHORIZATION) String authHeader);

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

    @POST("/api/provider/trip/{id}/started")
    Single<JsonElement> startOrder(
            @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
            @Path("id") String id);

//        @POST("api/provider/cancel")
//        Call<JsonObject> cancelOrder(
//                @Header(HEADER_KEY_AUTHORIZATION) String authHeader,
//                @Body CancelOrderRequestBody id);
//
//        class CancelOrderRequestBody {
//            @Expose
//            @SerializedName("id")
//            public String id;
//            @Expose(deserialize = false)
//            @SerializedName("cancel_reason")
//            public String cancelReason;
//        }

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
