package com.andar.hand.Retrofit;

import okhttp3.ResponseBody;
import retrofit2.Response;


public interface RetrofitCallback {
    public void Success(Response<ResponseBody> response);

    public void Failure(Throwable errorResponse);
}
