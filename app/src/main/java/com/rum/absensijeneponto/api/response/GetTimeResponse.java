package com.rum.absensijeneponto.api.response;

import com.google.gson.annotations.SerializedName;

public class GetTimeResponse {
    @SerializedName("time")
    private String time;

    public String getTime() {
        return time;
    }
}
