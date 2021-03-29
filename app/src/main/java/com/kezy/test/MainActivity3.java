package com.kezy.test;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.kezy.test.actionview.FloatingActionMenu;
import com.kezy.test.actionview.FloatingActionView;
import com.kezy.test.weight.LetoGameCenterFloatTipView;

import static com.kezy.test.actionview.FloatingActionView.POSITION_BOTTOM_RIGHT;


public class MainActivity3 extends AppCompatActivity {

    FloatingActionMenu actionMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView imageView1 = new ImageView(this);
        imageView1.setBackgroundResource(R.mipmap.imag);
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity3.this, "1111111", Toast.LENGTH_SHORT).show();
                actionMenu.close(true);
            }
        });

        ImageView imageView2 = new ImageView(this);
        imageView2.setBackgroundResource(R.mipmap.imag);
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity3.this, "222222", Toast.LENGTH_SHORT).show();
                actionMenu.close(true);
            }
        });

        ImageView imageView3 = new ImageView(this);
        imageView3.setBackgroundResource(R.mipmap.imag);
        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity3.this, "333333", Toast.LENGTH_SHORT).show();
                actionMenu.close(true);
            }
        });

        FloatingActionView.LayoutParams layoutParams = new FloatingActionView.LayoutParams(240, 240);
        layoutParams.rightMargin = 100;
        layoutParams.bottomMargin = 500;

        FloatingActionView actionButton = new FloatingActionView.Builder(this)
                .setPosition(POSITION_BOTTOM_RIGHT)
                .setLayoutParams(layoutParams)
                .build();


        actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(imageView1, 180, 180)
                .addSubActionView(imageView2, 180, 180)
                .addSubActionView(imageView3, 180, 180)
                .setRadius(500)
                .attachTo(actionButton)
                .build();


        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionMenu.isOpen()) {
                    actionMenu.close(true);
                } else {
                    actionMenu.open(true);
                }
            }
        });

        View decorView = getWindow().getDecorView();

        FrameLayout contentParent =
                (FrameLayout) decorView.findViewById(android.R.id.content);


        LetoGameCenterFloatTipView bottomBannerView = new LetoGameCenterFloatTipView(MainActivity3.this);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bottomBannerView.getLayoutParams();
        if (params == null) {
            params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }
        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        bottomBannerView.setLayoutParams(params);

        contentParent.addView(bottomBannerView);
    }
}