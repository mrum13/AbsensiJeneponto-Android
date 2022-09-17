package com.example.absensijeneponto.api;

import com.example.absensijeneponto.api.response.GetAbsenResponse;
import com.example.absensijeneponto.api.response.GetStatusAbsen;
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

    @FormUrlEncoded
    @POST("update_facecharacter")
    Call<Object> updateFace(
            @Header("Authorization") String token,
            @Field("face_character") String faceCharacter
    );

    @GET("status_absen_pegawai")
    Call<List<GetStatusAbsen>> getStatusAbsen(
            @Header("Authorization") String token
    );

    @FormUrlEncoded
    @POST("status_absen_pegawai")
    Call<List<Object>> updateStatusAbsen(
            @Header("Authorization") String token,
            @Field("absen_masuk") String absenMasuk,
            @Field("absen_keluar") String absenKeluar
    );

    @FormUrlEncoded
    @POST("absensi")
    Call<List<GetAbsenResponse>> getAbsenResponse(
            @Header("Authorization") String token,
            @Field("pegawai_id") String pegawaiId,
            @Field("periode") String periode,
            @Field("tanggal_absen") String tanggalAbsen,
            @Field("attendance_id") String attendanceId,
            @Field("waktu_masuk") String waktuMasuk,
            @Field("waktu_keluar") String waktuKeluar,
            @Field("keterangan") String keterangan,
            @Field("face_auth") String faceAuth,
            @Field("status_masuk") String statusMasuk,
            @Field("status_keluar") String statusKeluar
    );
}
