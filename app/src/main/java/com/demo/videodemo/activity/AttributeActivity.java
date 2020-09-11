package com.demo.videodemo.activity;

import android.content.Intent;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.videodemo.R;
import com.demo.videodemo.codec.BaseDecoder;
import com.demo.videodemo.codec.Frame;
import com.demo.videodemo.codec.IDecoderStateListener;
import com.demo.videodemo.codec.decoder.AudioDecoder;
import com.demo.videodemo.codec.decoder.VideoDecoder;
import com.demo.videodemo.utils.FileUtils;
import com.demo.videodemo.utils.ThreadPool;
import com.demo.videodemo.utils.TimeUtils;
import com.demo.videodemo.video.DanmukuVideoView;
import com.demo.videodemo.video.VideoView;
import com.demo.videodemo.video.abs.OnVideoViewStateChangeListener;
import com.demo.videodemo.video.controller.StandardVideoController;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttributeActivity extends AppCompatActivity {
    private static final String TAG = "AttributeActivity";
    private String path;
    private VideoDecoder mVideoDecoder;
    private AudioDecoder mAudioDecoder;
    private MediaFormat mMediaFormat;

    private final int PICK_CODE = 1;
    private DanmukuVideoView danmukuVideoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attribute);

        findViewById(R.id.select_video).setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
            startActivityForResult(intent, PICK_CODE);
        });
        danmukuVideoView = findViewById(R.id.player);
        danmukuVideoView.setLifecycleOwner(this);
        StandardVideoController standardVideoController = new StandardVideoController(this);
        standardVideoController.setTitle("测试");
        danmukuVideoView.setVideoController(standardVideoController);
        findViewById(R.id.parse_video).setOnClickListener(view -> parseVideo());
        findViewById(R.id.start_video).setOnClickListener(view -> {
            danmukuVideoView.setUrl(path);
            danmukuVideoView.start();
            startDecoder();
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
            findViewById(R.id.parse_video).setEnabled(true);
            findViewById(R.id.start_video).setEnabled(true);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void parseVideo() {
        StringBuilder stringBuilder = new StringBuilder();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        // 1. 获取duration
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        stringBuilder.append("duration(时长)=").append(TimeUtils.stringForTime(Integer.parseInt(duration))).append("\n");
        // 2. 视频高度
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        stringBuilder.append("height(高度)=").append(height).append("\n");
        // 3. 视频宽度
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        stringBuilder.append("width(宽度)=").append(width).append("\n");
        // 4. 码率
        String rating = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        int kbps = Integer.parseInt(rating) / 1000;
        stringBuilder.append("bitrate(平均码率)=").append(rating).append(" = ").append(kbps).append("kbps").append("\n");
        // 5. 旋转角度
        String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        stringBuilder.append("rotation(旋转角度)=").append(rotation).append("\n");
        // 6. 音轨数量
        String tracks = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS);
        stringBuilder.append("tracks(音轨数量)=").append(tracks).append("\n");
        // 7. mime类型
        String mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
        stringBuilder.append("mime=").append(mime).append("\n");
        // 8. fps
        String fps = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);
        stringBuilder.append("fps=").append(fps).append("\n");
        // 9. 关键帧的数量
        String count_of_I = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT);
        stringBuilder.append("count_of_I(关键帧数量)=").append(count_of_I).append("\n");
        // 10. title
        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        stringBuilder.append("title(标题)=").append(title).append("\n");
        // 11. date
        String date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
        stringBuilder.append("date(修改日期)=").append(date).append("\n");
        // 12. length
        stringBuilder.append("length(文件大小)=").append(FileUtils.calculateFileSize(new File(path))).append("\n");
        ((TextView) findViewById(R.id.attribute_tv)).setText(stringBuilder.toString());
    }

    private void parseFormat() {
        StringBuilder stringBuilder = new StringBuilder();
        // 1. 获取duration
        long duration = mMediaFormat.getLong(MediaFormat.KEY_DURATION);
        duration /= 1000;
        stringBuilder.append("duration(时长)=").append(TimeUtils.stringForTime((int) duration)).append("\n");
        // 2. 视频高度
        int height = mMediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
        stringBuilder.append("height(高度)=").append(height).append("\n");
        // 3. 视频宽度
        int width = mMediaFormat.getInteger(MediaFormat.KEY_WIDTH);
        stringBuilder.append("width(宽度)=").append(width).append("\n");
        // 4. 码率
//        int rating = mMediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);
//        int kbps = rating / 1000;
//        stringBuilder.append("bitrate(平均码率)=").append(rating).append(" = ").append(kbps).append("kbps").append("\n");
        // 5. 旋转角度
        int rotation = mMediaFormat.getInteger(MediaFormat.KEY_ROTATION);
        stringBuilder.append("rotation(旋转角度)=").append(rotation).append("\n");
        // 6. 音轨数量
//        int trackID = mMediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
//        stringBuilder.append("tracks(音轨数量)=").append(trackID).append("\n");
        // 7. mime类型
        String mime = mMediaFormat.getString(MediaFormat.KEY_MIME);
        stringBuilder.append("mime=").append(mime).append("\n");
        // 8. fps
        int fps = mMediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
        stringBuilder.append("fps=").append(fps).append("\n");
        // 9. 关键帧的数量
//        float count_of_I = mMediaFormat.getFloat(MediaFormat.KEY_I_FRAME_INTERVAL);
//        stringBuilder.append("count_of_I(关键帧数量)=").append(count_of_I).append("\n");
        // 10. sample_rate
//        int sample_rate = mMediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
//        stringBuilder.append("sample rate=").append(sample_rate).append("\n");
        // 11. capture rate
//        float capture_rate = mMediaFormat.getFloat(MediaFormat.KEY_CAPTURE_RATE);
//        stringBuilder.append("capture rate=").append(capture_rate).append("\n");
        // 12. length
        stringBuilder.append("length(文件大小)=").append(FileUtils.calculateFileSize(new File(path))).append("\n");
        ((TextView) findViewById(R.id.format_attribute)).setText(stringBuilder.toString());
    }


    private void startDecoder() {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        mVideoDecoder = new VideoDecoder(path, (SurfaceView) findViewById(R.id.surfaceView), null);
        mVideoDecoder.setStateListener(new IDecoderStateListener() {
            @Override
            public void decoderPrepare(BaseDecoder decoder) {

            }

            @Override
            public void decoderReady(BaseDecoder decoder) {
                mMediaFormat = decoder.getMediaFormat();
                if (mMediaFormat != null) ThreadPool.runOnUi(() -> parseFormat());
            }

            @Override
            public void decoderRunning(BaseDecoder decoder) {

            }

            @Override
            public void decoderPause(BaseDecoder decoder) {

            }

            @Override
            public void decodeOneFrame(BaseDecoder decoder, Frame frame) {
                if (mMediaFormat == null) mMediaFormat = decoder.getMediaFormat();
                if (mMediaFormat != null) ThreadPool.runOnUi(() -> parseFormat());
            }

            @Override
            public void decoderFinish(BaseDecoder decoder) {

            }

            @Override
            public void decoderDestroy(BaseDecoder decoder) {

            }

            @Override
            public void decoderError(BaseDecoder decoder, String msg) {

            }
        });
//        ThreadPool.runOnIOPool(mVideoDecoder);
//
//        mAudioDecoder = new AudioDecoder(path);
//        ThreadPool.runOnIOPool(mAudioDecoder);
        threadPool.execute(mVideoDecoder);

        mAudioDecoder = new AudioDecoder(path);
        threadPool.execute(mAudioDecoder);
    }
}
