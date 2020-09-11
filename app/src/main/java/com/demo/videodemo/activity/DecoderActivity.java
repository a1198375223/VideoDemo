package com.demo.videodemo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceView;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.videodemo.R;
import com.demo.videodemo.codec.BaseDecoder;
import com.demo.videodemo.codec.Frame;
import com.demo.videodemo.codec.IDecoderStateListener;
import com.demo.videodemo.codec.decoder.AudioDecoder;
import com.demo.videodemo.codec.decoder.FrameDecoder;
import com.demo.videodemo.codec.decoder.VideoDecoder;
import com.demo.videodemo.utils.ThreadPool;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DecoderActivity extends AppCompatActivity {

    private BaseDecoder mVideoDecoder;
    private AudioDecoder mAudioDecoder;
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/test.mp4";
    private ImageView mImageView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decoder);

        findViewById(R.id.button).setOnClickListener(v -> repack());
        mImageView = findViewById(R.id.image);

        initPlayer();
    }


    private void initPlayer() {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

//        mVideoDecoder = new VideoDecoder(path, (SurfaceView) findViewById(R.id.sfv), null);
        mVideoDecoder = new FrameDecoder(path);
        mVideoDecoder.setStateListener(new IDecoderStateListener() {
            @Override
            public void decoderPrepare(BaseDecoder decoder) {

            }

            @Override
            public void decoderReady(BaseDecoder decoder) {

            }

            @Override
            public void decoderRunning(BaseDecoder decoder) {

            }

            @Override
            public void decoderPause(BaseDecoder decoder) {

            }

            @Override
            public void decodeOneFrame(BaseDecoder decoder, Frame frame) {
                if (frame.image == null) return;
                ByteBuffer buffer = frame.image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                ThreadPool.runOnUi(() -> mImageView.setImageBitmap(bitmap));
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
        threadPool.execute(mVideoDecoder);

        mAudioDecoder = new AudioDecoder(path);
        threadPool.execute(mAudioDecoder);

        mVideoDecoder.resume();
        mAudioDecoder.resume();
    }

    private void repack() {
        /*val repack = MP4Repack(path)
        repack.start()*/
    }


    private static byte[] YUV_420_888toNV21(Image image) {
        byte[] nv21;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);
        return nv21;
    }
}
