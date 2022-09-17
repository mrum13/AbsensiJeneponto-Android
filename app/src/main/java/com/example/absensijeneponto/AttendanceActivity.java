package com.example.absensijeneponto;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.absensijeneponto.api.RetrofitClient;
import com.example.absensijeneponto.api.response.GetStatusAbsen;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceActivity extends AppCompatActivity {
    private ImageView imgBack;
    private Intent intent;
    private Button btnMasuk, btnPulang;
    private Date currentTime,timeIn,lateTimeIn,earlyTimeOut, timeOut;
    private String token, statusAbsenMasuk, statusAbsenPulang;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        token = Preferences.getToken(AttendanceActivity.this);
        statusAbsenMasuk ="";
        statusAbsenPulang ="";

        initComponent();

        disableBTNMasuk();
        disableBTNPulang();

        getStatusAbsen();


        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AttendanceActivity.this, MainActivity.class));
            }
        });

        //NOTED: ONLY DEBUG CODE BELOW
//        enableBTNMasuk();
//        enableBTNPulang();

        btnMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(AttendanceActivity.this, AttendanceActivity2.class);
                intent.putExtra("key_header", "Absen Masuk");
                startActivity(intent);
            }
        });

        btnPulang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(AttendanceActivity.this, AttendanceActivity2.class);
                intent.putExtra("key_header", "Absen Pulang");
                startActivity(intent);
            }
        });
    }

    void initComponent() {
        btnMasuk = findViewById(R.id.btn_absen_masuk);
        btnPulang = findViewById(R.id.btn_absen_pulang);
        imgBack = findViewById(R.id.img_back);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getTimeFunction(String a, String b){
        //penentuan jam absen masuk
        timeIn = new Date();
        Calendar jamMasuk = Calendar.getInstance();
        jamMasuk.setTime(timeIn);
        jamMasuk.set(Calendar.HOUR_OF_DAY, 7);
        jamMasuk.set(Calendar.MINUTE,30);
        jamMasuk.set(Calendar.SECOND,0);

        //penentuan jam absen masuk (terlambat)
        lateTimeIn = new Date();
        Calendar terlambatMasuk = Calendar.getInstance();
        terlambatMasuk.setTime(lateTimeIn);
        terlambatMasuk.set(Calendar.HOUR_OF_DAY, 10);
        terlambatMasuk.set(Calendar.MINUTE,0);
        terlambatMasuk.set(Calendar.SECOND,0);

        //penentuan jam absen pulang (pulang cepat)
        earlyTimeOut = new Date();
        Calendar cepatPulang = Calendar.getInstance();
        cepatPulang.setTime(earlyTimeOut);
        cepatPulang.set(Calendar.HOUR_OF_DAY, 14);
        cepatPulang.set(Calendar.MINUTE,0);
        cepatPulang.set(Calendar.SECOND,0);

        //penentuan jam absen pulang
        timeOut = new Date();
        Calendar jamPulang = Calendar.getInstance();
        jamPulang.setTime(timeOut);
        jamPulang.set(Calendar.HOUR_OF_DAY, 16);
        jamPulang.set(Calendar.MINUTE,0);
        jamPulang.set(Calendar.SECOND,0);

        currentTime = Calendar.getInstance().getTime();

        if (currentTime.before(jamMasuk.getTime())) {
            disableBTNMasuk();
            disableBTNPulang();
        }
        else if(currentTime.after(jamMasuk.getTime()) && currentTime.before(terlambatMasuk.getTime())) {
            if(a.equals("0") && b.equals("0")) {
                enableBTNMasuk();
                disableBTNPulang();
            } else if (a.equals("1") && b.equals("0")) {
                disableBTNMasuk();
                disableBTNPulang();
                Toast.makeText(AttendanceActivity.this, "Anda sudah melakukan absen masuk", Toast.LENGTH_SHORT).show();
            }
        } else if (currentTime.after(terlambatMasuk.getTime()) && currentTime.before(cepatPulang.getTime())){
            disableBTNMasuk();
            disableBTNPulang();
        } else if (currentTime.after(cepatPulang.getTime()) && currentTime.before(jamPulang.getTime())) {
            if (b.equals("0") && a.equals("0")) {
                Log.d("pulangg", "jalan di absen pulang tanpa absen masuk");
                Call<List<Object>> call= RetrofitClient.getApi().updateStatusAbsen("Bearer "+token, "1", "0");
                call.enqueue(new Callback<List<Object>>() {
                    @Override
                    public void onResponse(Call<List<Object>> call, Response<List<Object>> response) {

                    }

                    @Override
                    public void onFailure(Call<List<Object>> call, Throwable t) {
                        Toast.makeText(AttendanceActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                disableBTNMasuk();
                enableBTNPulang();
            }
            else if(b.equals("0") && a.equals("1")) {
                Log.d("pulangg", "jalan di absen pulang dengan absen masuk");
                disableBTNMasuk();
                enableBTNPulang();
            } else if (b.equals("1") && a.equals("1")) {
                disableBTNMasuk();
                disableBTNPulang();
                Toast.makeText(AttendanceActivity.this, "Anda sudah melakukan absen pulang", Toast.LENGTH_SHORT).show();
            }
        } else if (currentTime.after(jamPulang.getTime())) {
            if (b.equals("0") && a.equals("0")) {
                Log.d("pulangg", "jalan di absen pulang tanpa absen masuk");
                Call<List<Object>> call= RetrofitClient.getApi().updateStatusAbsen("Bearer "+token, "1", "0");
                call.enqueue(new Callback<List<Object>>() {
                    @Override
                    public void onResponse(Call<List<Object>> call, Response<List<Object>> response) {

                    }

                    @Override
                    public void onFailure(Call<List<Object>> call, Throwable t) {
                        Toast.makeText(AttendanceActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                disableBTNMasuk();
                enableBTNPulang();
            }
            else if(b.equals("0") && a.equals("1")) {
                Log.d("pulangg", "jalan di absen pulang dengan absen masuk");
                disableBTNMasuk();
                enableBTNPulang();
            } else if (b.equals("1") && a.equals("1")) {
                disableBTNMasuk();
                disableBTNPulang();
                Toast.makeText(AttendanceActivity.this, "Anda sudah melakukan absen pulang", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void getStatusAbsen() {
        Call<List<GetStatusAbsen>> call= RetrofitClient.getApi().getStatusAbsen("Bearer "+token);

        call.enqueue(new Callback<List<GetStatusAbsen>>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onResponse(Call<List<GetStatusAbsen>> call, Response<List<GetStatusAbsen>> response) {
                List<GetStatusAbsen> getStatusAbsen = response.body();

                if (response.isSuccessful()) {
                    getTimeFunction(getStatusAbsen.get(0).getAbsenMasuk(), getStatusAbsen.get(0).getAbsenKeluar());
                } else {
                    try {
                        JSONObject jobjError = new JSONObject(response.errorBody().string());
//                        Toast.makeText(getContext(), jobjError.getString("message"), Toast.LENGTH_LONG).show();
                        Toast.makeText(AttendanceActivity.this, "Gagal get status absen", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(AttendanceActivity.this, "error catch="+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GetStatusAbsen>> call, Throwable t) {
                Toast.makeText(AttendanceActivity.this, "onFailure="+String.valueOf(t), Toast.LENGTH_LONG).show();
            }
        });
    }

    void disableBTNMasuk() {
        btnMasuk.setEnabled(false);
        btnMasuk.setTextColor(Color.BLACK);
    }

    void disableBTNPulang() {
        btnPulang.setEnabled(false);
        btnPulang.setTextColor(Color.BLACK);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void enableBTNMasuk() {
        btnMasuk.setEnabled(true);
        btnMasuk.setTextColor(Color.WHITE);
        btnMasuk.setBackgroundColor(getColor(R.color.blue));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void enableBTNPulang() {
        btnPulang.setEnabled(true);
        btnPulang.setTextColor(Color.WHITE);
        btnPulang.setBackgroundColor(getColor(R.color.blue));
    }
}