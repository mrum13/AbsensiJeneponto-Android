package com.example.absensijeneponto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    FloatingActionButton btnScan;
    BottomNavigationView bottomNavigationView;
    Intent myIntent;
    String fromWhere;
    private boolean accessAttendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponent();
        getIntentFrom();

        //LocationManager
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);


        loadFragment(new HomeFragment());

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                        Toast.makeText(MainActivity.this, "Mohon Aktifkan GPS Anda !", Toast.LENGTH_SHORT).show();
                        accessAttendance = false;
                    } else {
                        accessAttendance =true;
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
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
                        myIntent = new Intent(MainActivity.this, AttendanceActivity.class);
                        startActivity(myIntent);
                    } else {
                        Toast.makeText(MainActivity.this, "Mohon aktifkan internet dan GPS ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    void initComponent() {
        accessAttendance = false;
        bottomNavigationView = findViewById(R.id.bottomNavbar);
        btnScan = findViewById(R.id.btn_scan);
    }

    void getIntentFrom() {
        fromWhere = getIntent().getStringExtra("from_where");
    }

    private boolean loadFragment(Fragment fragment){
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()){
            case R.id.mHome:
                fragment = new HomeFragment();
                break;
            case R.id.mAccount:
                fragment = new AccountFragment();
                break;
        }
        return loadFragment(fragment);
    }
}