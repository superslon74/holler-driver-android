package com.holler.app.Retrofit;

import okhttp3.ResponseBody;
import retrofit2.Response;

/*
* UNUSED?
*/

public interface RetrofitCallback {
    public void Success(Response<ResponseBody> response);

    public void Failure(Throwable errorResponse);
}
