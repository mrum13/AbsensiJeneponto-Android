package com.example.absensijeneponto;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.absensijeneponto.api.RetrofitClient;
import com.example.absensijeneponto.api.response.GetUserResponse;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    TextView tvNama, tvJabatan;
    String token;
    ConstraintLayout lytRegistFace, lytReport, lytReportTpp;
    Button btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        initComponent(view);

        token = Preferences.getToken(getContext());

        lytReportTpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.absensi-dinkes.my.id/login"));
                startActivity(browserIntent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), LoginActivity.class));
            }
        });

        lytRegistFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registFaceActivity = new Intent(getContext(), RegistFaceActivity.class);
                registFaceActivity.putExtra("from_where", "fromAccount");
                startActivity(registFaceActivity);
            }
        });

        lytReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reportActivity = new Intent(getContext(), ReportActivity.class);
                startActivity(reportActivity);
            }
        });

        Call<List<GetUserResponse>> call= RetrofitClient.getApi().getUser("Bearer "+token);
        call.enqueue(new Callback<List<GetUserResponse>>() {
            @Override
            public void onResponse(Call<List<GetUserResponse>> call, Response<List<GetUserResponse>> response) {
                List<GetUserResponse> data = response.body();

                if(response.isSuccessful()) {
                    tvNama.setText(data.get(0).getName());
                    tvJabatan.setText(data.get(0).getPegawai().getGolongan().getPangkat());

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
                Toast.makeText(getContext(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    void initComponent(View view) {
        tvNama = view.findViewById(R.id.tv_nama_pegawai_account);
        tvJabatan = view.findViewById(R.id.tv_jabatan_pegawai_account);
        lytRegistFace = view.findViewById(R.id.layout_option_account2);
        lytReport = view.findViewById(R.id.layout_option_account4);
        btnLogout = view.findViewById(R.id.btn_logout);
        lytReportTpp = view.findViewById(R.id.layout_option_account7);
    }
}