package com.demo.videodemo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import com.demo.videodemo.R;

public class MainActivity extends AppCompatActivity {

    private final int CODE = 0x01;
    private final String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.decoder).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DecoderActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.attribute).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AttributeActivity.class);
            startActivity(intent);
        });


        findViewById(R.id.frame).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FrameActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.frame2).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FrameActivity2.class);
            startActivity(intent);
        });
        requestPermissions(permissions, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}