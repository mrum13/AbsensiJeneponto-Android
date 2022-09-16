package com.example.absensijeneponto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class AttendanceActivity extends AppCompatActivity {
    private ImageView imgBack;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        imgBack = findViewById(R.id.img_back);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AttendanceActivity.this, MainActivity.class));
            }
        });
    }
}