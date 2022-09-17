package com.example.absensijeneponto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.absensijeneponto.api.RetrofitClient;
import com.example.absensijeneponto.api.response.GetAbsenResponse;
import com.example.absensijeneponto.api.response.GetUserResponse;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceActivity2 extends AppCompatActivity {
    private ImageView imgBack;
    private String header, keteranganAbsen, idJenisAbsen, idUser, token, namaPegawai, waktuAbsenMasuk, waktuAbsenPulang;
    private TextView tvHeader, timeDesc;
    private Date currentTime;
    private Date currentTimeDebug;
    private Date cTime;
    private Date tm1;
    private Date tm2;
    private Date tm3;
    private Date pc3;
    private Date pc2;
    private Date pc1;
    private ImageView imgTime;
    private CircularProgressIndicator circularProgressIndicator;

    private Calendar inLateEarly1;
    private Calendar inLateLast1;
    private Calendar inLateEarly2;
    private Calendar inLateLast2;
    private Calendar inLateEarly3;
    private Calendar inLateLast3;

    private Calendar outLateEarly1;
    private Calendar outLateLast1;
    private Calendar outLateEarly2;
    private Calendar outLateLast2;
    private Calendar outLateEarly3;
    private Calendar outLateLast3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance2);
        initComponent();

        token = Preferences.getToken(AttendanceActivity2.this);

        header = getIntent().getStringExtra("key_header");
        tvHeader.setText(header);

        circularProgressIndicator.setVisibility(View.GONE);

        getUser();
        getTimeFunction();

        getAllData();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AttendanceActivity2.this, AttendanceActivity.class));
            }
        });
    }

    void initComponent() {
        imgBack = findViewById(R.id.img_back);
        tvHeader = findViewById(R.id.tv_header);
        timeDesc = findViewById(R.id.tv_time_attendance);
        imgTime = findViewById(R.id.img_check_time);
        circularProgressIndicator = findViewById(R.id.circular_progress);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_presents:
                if (checked) {
                    if(timeDesc.getText().equals("Tepat Waktu")) {
                        keteranganAbsen = "Hadir - hadir";
                        idJenisAbsen = "3";
                    } else if (timeDesc.getText().equals("Terlambat 1") || timeDesc.getText().equals("Terlambat 2") || timeDesc.getText().equals("Terlambat 3")) {
                        keteranganAbsen = "Hadir - Telat Masuk";
                        idJenisAbsen = "8";
                    } else if (timeDesc.getText().equals("Pulang Cepat 1") || timeDesc.getText().equals("Pulang Cepat 2") || timeDesc.getText().equals("Pulang Cepat 3")) {
                        keteranganAbsen = "Hadir - Cepat Pulang";
                        idJenisAbsen = "7";
                    }
                }
                    break;
            case R.id.radio_permission:
                if (checked){
                    keteranganAbsen = "Tidak Hadir - Izin";
                    idJenisAbsen = "10";
                }
                    break;
            case R.id.radio_sick:
                if (checked) {
                    keteranganAbsen = "Tidak Hadir - Sakit";
                    idJenisAbsen = "5";
                }
                    break;
        }
    }

    void getUser() {
        Call<List<GetUserResponse>> call= RetrofitClient.getApi().getUser("Bearer "+token);

        call.enqueue(new Callback<List<GetUserResponse>>() {
            @Override
            public void onResponse(Call<List<GetUserResponse>> call, Response<List<GetUserResponse>> response) {
                if (response.isSuccessful()) {
                    List<GetUserResponse> data = response.body();

                    try {
                        getUserIDName(data.get(0).getPegawai().getUserId(), data.get(0).getName());
                    } catch (Exception e) {
                        Toast.makeText(AttendanceActivity2.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GetUserResponse>> call, Throwable t) {
                Toast.makeText(AttendanceActivity2.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void getTimeFunction() {


        tm1 = new Date();
        tm2 = new Date();
        tm3 = new Date();
        pc3 = new Date();
        pc2 = new Date();
        pc1 = new Date();


        inLateEarly1 = Calendar.getInstance();
        inLateLast1 = Calendar.getInstance();
        inLateEarly2 = Calendar.getInstance();
        inLateLast2 = Calendar.getInstance();
        inLateEarly3 = Calendar.getInstance();
        inLateLast3 = Calendar.getInstance();

        outLateEarly1 = Calendar.getInstance();
        outLateLast1 = Calendar.getInstance();
        outLateEarly2 = Calendar.getInstance();
        outLateLast2 = Calendar.getInstance();
        outLateEarly3 = Calendar.getInstance();
        outLateLast3 = Calendar.getInstance();

        cTime = new Date();
        Calendar currentTimeDebug =  Calendar.getInstance();
        //NOTED: ACTIVATED ONLY ON DEBUG MODE CODE BELOW to set time
//        currentTimeDebug.setTime(cTime);
//        currentTimeDebug.set(Calendar.HOUR_OF_DAY, 16);
//        currentTimeDebug.set(Calendar.MINUTE,0);
//        currentTimeDebug.set(Calendar.SECOND,0);

        batasAbsenMasuk();
        batasAbsenPulang();

        if (currentTimeDebug.getTime().before(inLateEarly1.getTime()) || currentTimeDebug.getTime().equals(inLateEarly1.getTime())) {
            timeDesc.setText("Tepat Waktu");
            imgTime.setImageResource(R.drawable.ic_check);
        } else if(currentTimeDebug.getTime().after(inLateEarly1.getTime()) && currentTimeDebug.getTime().before(inLateLast1.getTime())) {
            timeDesc.setText("Terlambat 1");
            imgTime.setImageResource(R.drawable.ic_failed);
        } else if(currentTimeDebug.getTime().equals(inLateEarly2.getTime()) || currentTimeDebug.getTime().after(inLateEarly2.getTime()) && currentTimeDebug.getTime().before(inLateLast2.getTime())) {
            timeDesc.setText("Terlambat 2");
            imgTime.setImageResource(R.drawable.ic_failed);
        }  else if(currentTimeDebug.getTime().equals(inLateEarly3.getTime()) || currentTimeDebug.getTime().after(inLateEarly3.getTime()) && currentTimeDebug.getTime().before(inLateLast3.getTime())) {
            timeDesc.setText("Terlambat 3");
            imgTime.setImageResource(R.drawable.ic_failed);
        }  else if(currentTimeDebug.getTime().equals(outLateEarly1.getTime()) || currentTimeDebug.getTime().after(outLateEarly1.getTime()) && currentTimeDebug.getTime().before(outLateLast1.getTime())) {
            timeDesc.setText("Pulang Cepat 1");
            imgTime.setImageResource(R.drawable.ic_failed);
        } else if(currentTimeDebug.getTime().equals(outLateEarly2.getTime()) || currentTimeDebug.getTime().after(outLateEarly2.getTime()) && currentTimeDebug.getTime().before(outLateLast2.getTime())) {
            timeDesc.setText("Pulang Cepat 2");
            imgTime.setImageResource(R.drawable.ic_failed);
        }  else if(currentTimeDebug.getTime().equals(outLateEarly3.getTime()) || currentTimeDebug.getTime().after(outLateEarly3.getTime()) && currentTimeDebug.getTime().before(outLateLast3.getTime())) {
            timeDesc.setText("Pulang Cepat 3");
            imgTime.setImageResource(R.drawable.ic_failed);
        } else if (currentTimeDebug.getTime().after(outLateLast1.getTime()) || currentTimeDebug.getTime().equals(outLateLast1.getTime())) {
            timeDesc.setText("Tepat Waktu");
            imgTime.setImageResource(R.drawable.ic_check);
        }
    }

    void getUserIDName(String id, String name) {
        idUser = id;
        namaPegawai = name;
    }

    void getWaktuMasukdanKeluar(){
        if (tvHeader.getText().equals("Absen Masuk")) {
            waktuAbsenMasuk = "00:00";
            waktuAbsenPulang = "00:00";
        } else if (tvHeader.getText().equals("Absen Pulang")) {
            waktuAbsenMasuk = Preferences.getWaktuMasuk(AttendanceActivity2.this);


        }
    }

    void getAllData() {

        //periode (November-2022)

        //tangal absen (2022-11-12)

        //face

        //locationAuth

        //id jenis absen ADA

        //pegawaiId ADA

        //waktumasuk ADA

        //waktukeluar ADA

        //keteranganAbsen ADA

        //statusMasuk ADA

        //statusKeluar ADA


    }

    void postAbsen(String pegawaiId, String periode, String tanggalAbsen, String attendanceId, String waktuMasuk, String waktuKeluar, String keterangan, String faceAuth, String statusMasuk, String statusKeluar) {
        Call<List<GetAbsenResponse>> call= RetrofitClient.getApi().getAbsenResponse("Bearer "+token, pegawaiId, periode, tanggalAbsen, attendanceId, waktuMasuk, waktuKeluar, keterangan, faceAuth, statusMasuk, statusKeluar);

        call.enqueue(new Callback<List<GetAbsenResponse>>() {
            @Override
            public void onResponse(Call<List<GetAbsenResponse>> call, Response<List<GetAbsenResponse>> response) {
                if(response.isSuccessful()) {
                    List<GetAbsenResponse> data = response.body();

                    Preferences.setWaktuMasuk(getApplicationContext(),data.get(0).getWaktuMasuk());
                }
            }

            @Override
            public void onFailure(Call<List<GetAbsenResponse>> call, Throwable t) {

            }
        });
    }
    
    void batasAbsenMasuk() {
        inLateEarly1.setTime(tm1);
        inLateEarly1.set(Calendar.HOUR_OF_DAY, 8);
        inLateEarly1.set(Calendar.MINUTE,00);
        inLateEarly1.set(Calendar.SECOND,01);

        inLateLast1.setTime(tm1);
        inLateLast1.set(Calendar.HOUR_OF_DAY, 8);
        inLateLast1.set(Calendar.MINUTE,31);
        inLateLast1.set(Calendar.SECOND,00);

        inLateEarly2.setTime(tm2);
        inLateEarly2.set(Calendar.HOUR_OF_DAY, 8);
        inLateEarly2.set(Calendar.MINUTE,31);
        inLateEarly2.set(Calendar.SECOND,0);

        inLateLast2.setTime(tm2);
        inLateLast2.set(Calendar.HOUR_OF_DAY, 9);
        inLateLast2.set(Calendar.MINUTE,00);
        inLateLast2.set(Calendar.SECOND,00);

        inLateEarly3.setTime(tm3);
        inLateEarly3.set(Calendar.HOUR_OF_DAY, 9);
        inLateEarly3.set(Calendar.MINUTE,1);
        inLateEarly3.set(Calendar.SECOND,0);

        inLateLast3.setTime(tm3);
        inLateLast3.set(Calendar.HOUR_OF_DAY, 10);
        inLateLast3.set(Calendar.MINUTE,01);
        inLateLast3.set(Calendar.SECOND,00);
    }

    void batasAbsenPulang() {
        outLateEarly1.setTime(pc1);
        outLateEarly1.set(Calendar.HOUR_OF_DAY, 15);
        outLateEarly1.set(Calendar.MINUTE,30);
        outLateEarly1.set(Calendar.SECOND,00);

        outLateLast1.setTime(pc1);
        outLateLast1.set(Calendar.HOUR_OF_DAY, 16);
        outLateLast1.set(Calendar.MINUTE,00);
        outLateLast1.set(Calendar.SECOND,00);

        outLateEarly2.setTime(pc2);
        outLateEarly2.set(Calendar.HOUR_OF_DAY, 15);
        outLateEarly2.set(Calendar.MINUTE,00);
        outLateEarly2.set(Calendar.SECOND,0);

        outLateLast2.setTime(pc2);
        outLateLast2.set(Calendar.HOUR_OF_DAY, 15);
        outLateLast2.set(Calendar.MINUTE,30);
        outLateLast2.set(Calendar.SECOND,00);

        outLateEarly3.setTime(pc3);
        outLateEarly3.set(Calendar.HOUR_OF_DAY, 14);
        outLateEarly3.set(Calendar.MINUTE,0);
        outLateEarly3.set(Calendar.SECOND,0);

        outLateLast3.setTime(pc3);
        outLateLast3.set(Calendar.HOUR_OF_DAY, 15);
        outLateLast3.set(Calendar.MINUTE,00);
        outLateLast3.set(Calendar.SECOND,00);
    }
}