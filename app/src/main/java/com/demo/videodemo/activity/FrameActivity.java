package com.demo.videodemo.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.videodemo.R;
import com.demo.videodemo.codec.extractor.FrameExtractor;
import com.demo.videodemo.utils.FileUtils;
import com.demo.videodemo.utils.ThreadPool;

public class FrameActivity extends AppCompatActivity {
    private static final String TAG = "FrameActivity";
    private final int PICK_CODE = 1;
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/test.mp4";
    private FrameExtractor mExtractor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);
//        mExtractor = new FrameExtractor(path);

        findViewById(R.id.select_video).setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
            startActivityForResult(intent, PICK_CODE);
        });

        findViewById(R.id.get_frame).setOnClickListener(view -> {
            String time = ((EditText) findViewById(R.id.frame_time_input)).getText().toString();
            Log.d(TAG, "get frame at " + Long.parseLong(time));
            new Thread(() -> {
                mExtractor = new FrameExtractor(path);
                long startTs = System.currentTimeMillis();
                Bitmap bitmap = mExtractor.getFrameAtTime(Long.parseLong(time) * 1000);
                long endTs = System.currentTimeMillis();

                ThreadPool.runOnUi(() -> {
                    ((ImageView) findViewById(R.id.frame_image)).setImageBitmap(bitmap);
                    ((TextView) findViewById(R.id.time)).setText((endTs - startTs) + "ms");
                });
            }).start();

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == PICK_CODE) {
            if (data == null) {
                Log.e(TAG, "onActivityResult: data == null");
                finish();
                return;
            }
            Uri selectedVideo = data.getData();
            if (selectedVideo == null) {
                Toast.makeText(this, "请重新选择", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "onActivityResult: uri=" + selectedVideo.toString());

            path = FileUtils.getPath(this, selectedVideo);
            ((TextView) findViewById(R.id.path)).setText("path=" + path);
            Log.d(TAG, "onActivityResult: video path=" + path);

//            mExtractor = new FrameExtractor(path);
            findViewById(R.id.get_frame).setEnabled(true);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
