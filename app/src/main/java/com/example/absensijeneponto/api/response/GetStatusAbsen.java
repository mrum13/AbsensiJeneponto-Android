package com.example.absensijeneponto.api.response;

import com.google.gson.annotations.SerializedName;

public class GetStatusAbsen {
    @SerializedName("absen_masuk")
    private String absenMasuk;
    @SerializedName("absen_keluar")
    private String absenKeluar;

    public String getAbsenMasuk() {
        return absenMasuk;
    }

    public String getAbsenKeluar() {
        return absenKeluar;
    }
}
