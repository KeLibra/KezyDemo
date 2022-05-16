package com.kezy.danmuview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {


    private DanMuView danMuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        danMuView = findViewById(R.id.danmu_view);

        danMuView.startPlay();
    }
}