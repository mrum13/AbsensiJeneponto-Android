package com.example.absensijeneponto;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.absensijeneponto.api.RetrofitClient;
import com.example.absensijeneponto.api.response.GetUserResponse;
import com.example.absensijeneponto.api.response.LoginResponse;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    TextView tvNama, tvNip, tvAbsenMasuk, tvAbsenPulang;
    String token;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initComponent(view);

        token = Preferences.getToken(getContext());

        Call<List<GetUserResponse>> call= RetrofitClient.getApi().getUser("Bearer "+token);

        call.enqueue(new Callback<List<GetUserResponse>>() {
            @Override
            public void onResponse(Call<List<GetUserResponse>> call, Response<List<GetUserResponse>> response) {
                List<GetUserResponse> getUserResponse = response.body();

                if (response.isSuccessful()) {
                    tvNama.setText(getUserResponse.get(0).getName());
                    tvNip.setText(getUserResponse.get(0).getPegawai().getNip());


                } else {
                    try {
                        JSONObject jobjError = new JSONObject(response.errorBody().string());
//                        Toast.makeText(getContext(), jobjError.getString("message"), Toast.LENGTH_LONG).show();
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "error catch="+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GetUserResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "onFailure="+String.valueOf(t), Toast.LENGTH_LONG).show();

            }
        });

        return view;
    }

    void initComponent(View view) {
        tvNama = view.findViewById(R.id.tv_nama);
        tvNip = view.findViewById(R.id.tv_nip);
        tvAbsenMasuk = view.findViewById(R.id.tv_absen_masuk);
        tvAbsenPulang = view.findViewById(R.id.tv_absen_pulang);
    }
}