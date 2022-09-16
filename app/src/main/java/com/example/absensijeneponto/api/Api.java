package com.example.absensijeneponto.api;

import com.example.absensijeneponto.api.response.GetUserResponse;
import com.example.absensijeneponto.api.response.LoginResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface Api {
    @FormUrlEncoded
    @POST("auth/login")
    Call<LoginResponse> userLogin(
            @Field("email") String email,
            @Field("password") String password
    );

    @GET("detail_pegawai")
    Call<List<GetUserResponse>> getUser(
            @Header("Authorization") String token
    );
}
