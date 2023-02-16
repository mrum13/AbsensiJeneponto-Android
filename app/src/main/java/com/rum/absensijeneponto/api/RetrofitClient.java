package com.rum.absensijeneponto.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL="https://absensi-dinkes.my.id/api/v1/";
    private static  RetrofitClient mInstance;
    private final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static final Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create());

    public static Retrofit retrofit() {
        return builder.build();
    }

    public static Api getApi(){
        return retrofit().create(Api.class);
    }

}
