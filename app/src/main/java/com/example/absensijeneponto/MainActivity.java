package com.example.absensijeneponto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    FloatingActionButton btnScan;
    BottomNavigationView bottomNavigationView;
    Intent myIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavbar);
        btnScan = findViewById(R.id.btn_scan);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);


        loadFragment(new HomeFragment());

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myIntent = new Intent(MainActivity.this, AttendanceActivity.class);
                startActivity(myIntent);
            }
        });
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