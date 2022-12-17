package com.example.absensijeneponto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
    private boolean accessAttendance;
    private CardView cardFakeGPS;
    private TextView tvCardFake;

    private String nip;
    private String pass;

    private String fake1;
    private String fake2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btn_login);
        etNip = findViewById(R.id.et_nip);
        etPassword = findViewById(R.id.et_pass);
        cardFakeGPS = findViewById(R.id.card_fake_gps);
        circularProgressIndicator = findViewById(R.id.circular_progress);
        tvCardFake = findViewById(R.id.tv_card_fake);

        fake1 = "Hapus aplikasi terkait fake gps !";
        fake2 = "Matikan Opsi Pengembang atau Developer Mode !";

        cardFakeGPS.setVisibility(View.GONE);

        //LocationManager
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //cek mock dan app menggunakan mock
        isMockSettingsON(this);
        areThereMockPermissionApps(this);

        int adb = Settings.Secure.getInt(this.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED , 0);



        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                Toast.makeText(LoginActivity.this, "Mohon Aktifkan GPS Anda !", Toast.LENGTH_SHORT).show();
                accessAttendance = false;
            } else {
                accessAttendance =true;
            }
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        if (accessAttendance==true) {
            boolean gps_enabled = false;
            boolean network_enabled = false;

            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch(Exception ex) {}

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ex) {}

            if(gps_enabled && network_enabled) {
                if(isMockSettingsON(LoginActivity.this)==true && areThereMockPermissionApps(LoginActivity.this)==true) {
//                    loadingLayout.setVisibility(View.GONE);
                    btnLogin.setEnabled(false);
                    cardFakeGPS.setVisibility(View.VISIBLE);
                    tvCardFake.setText(fake1);
                } else if (isMockSettingsON(LoginActivity.this)==true && areThereMockPermissionApps(LoginActivity.this)==false){
//                    loadingLayout.setVisibility(View.GONE);
                    btnLogin.setEnabled(false);
                    cardFakeGPS.setVisibility(View.VISIBLE);
                    tvCardFake.setText(fake1);
                } else if (isMockSettingsON(LoginActivity.this)==false && areThereMockPermissionApps(LoginActivity.this)==true) {
//                    loadingLayout.setVisibility(View.GONE);
                    btnLogin.setEnabled(false);
                    cardFakeGPS.setVisibility(View.VISIBLE);
                    tvCardFake.setText(fake1);
                } else {
                    if (adb==1) {
                        btnLogin.setEnabled(false);
                        cardFakeGPS.setVisibility(View.VISIBLE);
                        tvCardFake.setText(fake2);
                    }else {
                        cardFakeGPS.setVisibility(View.GONE);
                        tvCardFake.setText(fake1);
                    }
                }
            } else {
                Toast.makeText(LoginActivity.this, "Mohon aktifkan internet dan GPS ", Toast.LENGTH_SHORT).show();
//                loadingLayout.setVisibility(View.GONE);
            }

            if(gps_enabled) {
//                myIntent = new Intent(MainActivity.this, AttendanceActivity.class);
//                startActivity(myIntent);
            } else {
                Toast.makeText(LoginActivity.this, "Mohon aktifkan internet dan GPS ", Toast.LENGTH_SHORT).show();
            }
        }


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

    //mengecek apakah settingan MOCK aktif atau tidak
    public static boolean isMockSettingsON(Context context) {
        // returns true if mock location enabled, false if not enabled.
        if (Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
            return false;
        else
            return true;
    }

    public static boolean areThereMockPermissionApps(Context context) {
        int count = 0;

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS);

                // Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        if (requestedPermissions[i].equals("android.permission.ACCESS_MOCK_LOCATION")) {
                            if (applicationInfo.packageName.contains("fake")
                                    || applicationInfo.packageName.contains("gps")
                                    || applicationInfo.packageName.contains("maps")
                                    || applicationInfo.packageName.contains("earth")
                                    || applicationInfo.packageName.contains("spoof")
                                    || applicationInfo.packageName.contains("mock")
                                    || applicationInfo.packageName.contains("location")
                                    || applicationInfo.packageName.contains("change")
                                    || applicationInfo.packageName.contains("track")
                                    || applicationInfo.packageName.contains("log")
                                    || applicationInfo.packageName.contains("root")
                            ) {
                                count++;
                            } else {

                            }
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("Got exception " , e.getMessage());
            }
        }

        if (count > 0)
            return true;
        return false;
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