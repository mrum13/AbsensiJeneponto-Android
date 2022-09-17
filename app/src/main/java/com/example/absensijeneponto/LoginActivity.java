package com.example.absensijeneponto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.absensijeneponto.api.RetrofitClient;
import com.example.absensijeneponto.api.response.GetUserResponse;
import com.example.absensijeneponto.api.response.LoginResponse;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private Button btnLogin;
    private Intent intent;
    private EditText etNip;
    private EditText etPassword;
    private CircularProgressIndicator circularProgressIndicator;

    private String nip;
    private String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btn_login);
        etNip = findViewById(R.id.et_nip);
        etPassword = findViewById(R.id.et_pass);
        circularProgressIndicator = findViewById(R.id.circular_progress);



        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonLoading();

                nip = etNip.getText().toString();
                pass = etPassword.getText().toString();

                if (nip.equals("") || pass.equals("")) {
                    Toast.makeText(LoginActivity.this, "Mohon isi form NIP dan Password !", Toast.LENGTH_SHORT).show();
                } else {
                    Call<LoginResponse> call= RetrofitClient.getApi().userLogin(nip,pass);

                    call.enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                            LoginResponse loginResponse = response.body();

                            if (response.isSuccessful()) {
                                Preferences.setToken(getApplicationContext(),loginResponse.getToken());
                                Toast.makeText(LoginActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();


                                checkFaceData();

                                buttonFinishedLoading();

                            } else {
                                try {
                                    JSONObject jobjError = new JSONObject(response.errorBody().string());
                                    Toast.makeText(LoginActivity.this, jobjError.getString("message"), Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(LoginActivity.this, "error catch="+e.getMessage(), Toast.LENGTH_LONG).show();
                                }

                                buttonFinishedLoading();
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
//                            Log.d("failure", t.toString());
                            Toast.makeText(LoginActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
                            btnLogin.setVisibility(View.VISIBLE);
                            circularProgressIndicator.setVisibility(View.GONE);
                        }
                    });
                }


            }
        });
    }

    void buttonLoading() {
        btnLogin.setVisibility(View.GONE);
        circularProgressIndicator.setVisibility(View.VISIBLE);
    }
    void buttonFinishedLoading() {
        btnLogin.setVisibility(View.VISIBLE);
        circularProgressIndicator.setVisibility(View.GONE);
    }

    void checkFaceData() {
        String token = Preferences.getToken(getApplicationContext());
        Call<List<GetUserResponse>> call= RetrofitClient.getApi().getUser("Bearer "+token);

        call.enqueue(new Callback<List<GetUserResponse>>() {
            @Override
            public void onResponse(Call<List<GetUserResponse>> call, Response<List<GetUserResponse>> response) {
                List<GetUserResponse> getUserResponse = response.body();

                if (response.isSuccessful()) {
                    if (getUserResponse.get(0).getPegawai().getFace_character()==null || getUserResponse.get(0).getPegawai().getFace_character().equals("null")) {
                        intent = new Intent(LoginActivity.this, RegistFaceActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                    }

                    intent.putExtra("from_where", "fromLogin");
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        JSONObject jobjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(LoginActivity.this, jobjError.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GetUserResponse>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}