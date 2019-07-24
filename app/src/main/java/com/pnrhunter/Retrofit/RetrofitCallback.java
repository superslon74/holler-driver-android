package com.pnrhunter.Retrofit;

import okhttp3.ResponseBody;
import retrofit2.Response;

/*
* UNUSED?
*/

@Deprecated
public interface RetrofitCallback {
    public void Success(Response<ResponseBody> response);

    public void Failure(Throwable errorResponse);
}
