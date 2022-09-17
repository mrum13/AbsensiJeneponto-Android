package com.example.absensijeneponto.api.response;

import com.google.gson.annotations.SerializedName;

public class GetAbsenResponse {
    @SerializedName("id")
    private int id;
    @SerializedName("pegawai_id")
    private String pegawaiId;
    @SerializedName("periode")
    private String periode;
    @SerializedName("attendance_id")
    private String attendanceId;
    @SerializedName("tanggal_absen")
    private String tanggalAbsen;
    @SerializedName("jumlah_lembur")
    private String jumlahLembur;
    @SerializedName("waktu_masuk")
    private String waktuMasuk;
    @SerializedName("waktu_keluar")
    private String waktuKeluar;
    @SerializedName("location_auth")
    private String locationAuth;
    @SerializedName("face_auth")
    private String faceAuth;
    @SerializedName("keterangan")
    private String keterangan;
    @SerializedName("status_masuk")
    private String statusMasuk;
    @SerializedName("status_keluar")
    private String statusKeluar;

    public int getId() {
        return id;
    }

    public String getPegawaiId() {
        return pegawaiId;
    }

    public String getPeriode() {
        return periode;
    }

    public String getAttendanceId() {
        return attendanceId;
    }

    public String getTanggalAbsen() {
        return tanggalAbsen;
    }

    public String getJumlahLembur() {
        return jumlahLembur;
    }

    public String getWaktuMasuk() {
        return waktuMasuk;
    }

    public String getWaktuKeluar() {
        return waktuKeluar;
    }

    public String getLocationAuth() {
        return locationAuth;
    }

    public String getFaceAuth() {
        return faceAuth;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public String getStatusMasuk() {
        return statusMasuk;
    }

    public String getStatusKeluar() {
        return statusKeluar;
    }
}
