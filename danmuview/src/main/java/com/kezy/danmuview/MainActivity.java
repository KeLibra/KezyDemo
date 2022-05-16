package com.kezy.danmuview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    private DanMuView danMuView;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.btn_click);
        danMuView = findViewById(R.id.danmu_view);
        danMuView.setMaxItem(2);
        danMuView.setDelayTime(2500);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (danMuView.isPlaying()) {
                    danMuView.stopPlay();
                } else {
                    danMuView.startPlay();
                }
            }
        });
    }
}