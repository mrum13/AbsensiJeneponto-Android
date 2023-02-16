package com.rum.absensijeneponto;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rum.absensijeneponto.api.RetrofitClient;
import com.rum.absensijeneponto.api.errorresponses.ApiError;
import com.rum.absensijeneponto.api.errorresponses.ErrorUtils;
import com.rum.absensijeneponto.api.response.GetStatusAbsen;
import com.rum.absensijeneponto.api.response.GetUserResponse;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    TextView tvKeteranganAbsen, tvNama, tvNip, tvAbsenMasuk, tvAbsenPulang;
    Button btnAbsenHome;
    String token, absenMasuk, absenKeluar;
    int kondisiAbsen;
    Intent myIntent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initComponent(view);

        absenMasuk="";
        absenKeluar="";
        kondisiAbsen=0;

        token = Preferences.getToken(getContext());

        getUser();
        getStatusAbsen();


        btnAbsenHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myIntent = new Intent(getContext(), AttendanceActivity.class);
                startActivity(myIntent);
            }
        });

        return view;
    }

    void initComponent(View view) {
        tvNama = view.findViewById(R.id.tv_nama);
        tvNip = view.findViewById(R.id.tv_nip);
        tvAbsenMasuk = view.findViewById(R.id.tv_absen_masuk);
        tvAbsenPulang = view.findViewById(R.id.tv_absen_pulang);
        tvKeteranganAbsen = view.findViewById(R.id.tv_keterangan_absen);
        btnAbsenHome = view.findViewById(R.id.btn_absen_home);
    }

    void getStatusAbsen() {
        Call<List<GetStatusAbsen>> call= RetrofitClient.getApi().getStatusAbsen("Bearer "+token);

        call.enqueue(new Callback<List<GetStatusAbsen>>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onResponse(Call<List<GetStatusAbsen>> call, Response<List<GetStatusAbsen>> response) {
                List<GetStatusAbsen> getStatusAbsen = response.body();

                if (response.isSuccessful()) {
                    absenMasuk = getStatusAbsen.get(0).getAbsenMasuk();
                    absenKeluar = getStatusAbsen.get(0).getAbsenKeluar();

                    if (absenMasuk.equals("0") && absenKeluar.equals("0")) {
                        tvKeteranganAbsen.setText("Anda belum absen hari ini !");
                        kondisiAbsen = 0;
                    } else if (absenMasuk.equals("1") && absenKeluar.equals("0")) {
                        tvKeteranganAbsen.setText("Anda sudah melakukan ABSEN MASUK hari ini");
                        tvKeteranganAbsen.setTextColor(0xff0060AF);
                        kondisiAbsen = 1;
                    } else if (absenMasuk.equals("1") && absenKeluar.equals("1")) {
                        tvKeteranganAbsen.setText("Anda sudah melakukan ABSEN hari ini");
                        tvKeteranganAbsen.setTextColor(0xff50CD89);
                        btnAbsenHome.setEnabled(false);
                        kondisiAbsen = 2;
                    }

                    if (kondisiAbsen==0) {
                        tvAbsenMasuk.setText("Belum absen !");
                        tvAbsenPulang.setText("Belum absen !");

                        tvAbsenMasuk.setTextColor(getResources().getColor(R.color.red));
                        tvAbsenMasuk.setTextColor(getResources().getColor(R.color.red));
                    } else if (kondisiAbsen==1) {
                        tvAbsenMasuk.setText("Sudah absen");
                        tvAbsenPulang.setText("Belum absen !");

                        tvAbsenMasuk.setTextColor(getResources().getColor(R.color.green));
                        tvAbsenMasuk.setTextColor(getResources().getColor(R.color.red));
                    } else if (kondisiAbsen==2) {
                        tvAbsenMasuk.setText("Sudah absen");
                        tvAbsenPulang.setText("Sudah absen");

                        tvAbsenMasuk.setTextColor(getResources().getColor(R.color.green));
                        tvAbsenMasuk.setTextColor(getResources().getColor(R.color.green));
                    } else {
                        tvAbsenMasuk.setText("-");
                        tvAbsenPulang.setText("-");

                        tvAbsenMasuk.setTextColor(getResources().getColor(R.color.grey));
                        tvAbsenMasuk.setTextColor(getResources().getColor(R.color.grey));
                    }
                } else {
                    try {
                        ApiError error = ErrorUtils.parseError(response);
                        Toast.makeText(getContext(), error.message(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "error catch="+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GetStatusAbsen>> call, Throwable t) {
                Toast.makeText(getContext(), "onFailure="+String.valueOf(t), Toast.LENGTH_LONG).show();
            }
        });
    }

    void getUser() {
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
                        ApiError error = ErrorUtils.parseError(response);
                        Toast.makeText(getContext(), error.message(), Toast.LENGTH_SHORT).show();
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
    }
}