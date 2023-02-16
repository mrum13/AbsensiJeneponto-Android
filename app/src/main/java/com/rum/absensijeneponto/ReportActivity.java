package com.rum.absensijeneponto;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.rum.absensijeneponto.api.RetrofitClient;
import com.rum.absensijeneponto.api.response.GetUserResponse;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportActivity extends AppCompatActivity {
    private EditText etFirstDate, etLastDate;
    private ImageView btnBack;
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;
    private Button btnSeeReport;
    private String firstDate, lastDate, token;
    private int idUser;
    CircularProgressIndicator circularProgressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        token = Preferences.getToken(ReportActivity.this);

        btnBack = findViewById(R.id.ic_back);
        etFirstDate = findViewById(R.id.et_date_first_activity);
        etLastDate = findViewById(R.id.et_date_last_activity);
        btnSeeReport = findViewById(R.id.btn_see_report_activity);
        circularProgressIndicator = findViewById(R.id.circular_progress);

        circularProgressIndicator.setVisibility(View.GONE);

        firstDate = "kosong";
        lastDate = "kosong";
        idUser = 0;

        getUserId();

        etFirstDate.setFocusable(false);
        etFirstDate.setClickable(true);
        etLastDate.setFocusable(false);
        etLastDate.setClickable(true);

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        etFirstDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popFirstDatePicker();
            }
        });

        etLastDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popLastDatePicker();
            }
        });

        btnSeeReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setGoneCircularProgress(true);
                checkForm();
            }
        });
    }

    private void getUserId() {
        Call<List<GetUserResponse>> getUser = RetrofitClient.getApi().getUser("Bearer "+token);
        getUser.enqueue(new Callback<List<GetUserResponse>>() {
            @Override
            public void onResponse(Call<List<GetUserResponse>> call, Response<List<GetUserResponse>> response) {
                List<GetUserResponse> getUserResponse = response.body();
                if (response.isSuccessful()) {
                    idUser = getUserResponse.get(0).getId();
                } else {
                    Toast.makeText(ReportActivity.this, "Gagal Get ID User", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GetUserResponse>> call, Throwable t) {
                Toast.makeText(ReportActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkForm() {
        if (firstDate.equals("kosong") || lastDate.equals("kosong")) {
            Toast.makeText(this, "Mohon lengkapi tanggal laporan", Toast.LENGTH_SHORT).show();
            setGoneCircularProgress(false);
        } else {
            Intent intentPdfViewer = new Intent(ReportActivity.this, WebviewReportActivity.class);
            intentPdfViewer.putExtra("first_date", firstDate);
            intentPdfViewer.putExtra("last_date", lastDate);
            intentPdfViewer.putExtra("idUser", idUser);
            startActivity(intentPdfViewer);
            setGoneCircularProgress(false);
        }
    }

    private void popFirstDatePicker() {
        Calendar calendar = Calendar.getInstance();

        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                /**
                 * Update TextView dengan tanggal yang kita pilih
                 */
                etFirstDate.setText(dateFormatter.format(newDate.getTime()));
                firstDate = etFirstDate.getText().toString();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void popLastDatePicker() {
        Calendar calendar = Calendar.getInstance();

        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                /**
                 * Update TextView dengan tanggal yang kita pilih
                 */
                etLastDate.setText(dateFormatter.format(newDate.getTime()));
                lastDate = etLastDate.getText().toString();

            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setGoneCircularProgress(boolean condition) {
        if (condition==false) {
            circularProgressIndicator.setVisibility(View.GONE);
            btnSeeReport.setEnabled(true);
        } else {
            circularProgressIndicator.setVisibility(View.VISIBLE);
            btnSeeReport.setEnabled(false);
        }
    }
}