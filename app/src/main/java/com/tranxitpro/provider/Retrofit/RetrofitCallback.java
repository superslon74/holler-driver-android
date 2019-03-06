package com.tranxitpro.provider.Retrofit;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */

public interface RetrofitCallback {
    public void Success(Response<ResponseBody> response);

    public void Failure(Throwable errorResponse);
}
