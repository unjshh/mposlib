package com.cxycxx.mposcore.http;

import com.google.gson.JsonObject;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Retrofit的POST接口
 */

public interface RetrofitPost {
    @POST
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
        //添加
    Call<JsonObject> post(@Url String url, @Body Object data);

    @POST
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
        //添加
    Observable<JsonObject> rxPost(@Url String url, @Body Object data);

    @POST
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
        //添加
    Observable<JsonObject> rxPost2(@Url String url, @Body RequestBody data);


    @GET
    Call<String> getCall(@Url String url, @HeaderMap Map<String, String> headers);

    @POST
    Call<String> postCall(@Url String url, @Body RequestBody data, @HeaderMap Map<String, String> headers);
}
