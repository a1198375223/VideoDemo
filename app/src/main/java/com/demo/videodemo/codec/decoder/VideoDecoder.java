package com.demo.videodemo.codec.decoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.demo.videodemo.codec.BaseDecoder;
import com.demo.videodemo.codec.IExtractor;
import com.demo.videodemo.codec.extractor.VideoExtractor;

import java.nio.ByteBuffer;

public class VideoDecoder extends BaseDecoder {
    private SurfaceView mSurfaceView;
    private Surface mSurface;

    public VideoDecoder(String filePath, SurfaceView surfaceView, Surface surface) {
        super(filePath);
        this.mSurfaceView = surfaceView;
        this.mSurface = surface;
    }

    @Override
    public IExtractor createExtractor(String path) {
        return new VideoExtractor(path);
    }

    @Override
    public void initParams(MediaFormat format) {

    }

    @Override
    public boolean configCodec(final MediaCodec codec, final MediaFormat format) {
        if (mSurface != null) {
            codec.configure(format, mSurface, null, 0);
            // notifyDecode()
        } else if (mSurfaceView != null && mSurfaceView.getHolder() != null &&
                mSurfaceView.getHolder().getSurface() != null) {
            mSurface = mSurfaceView.getHolder().getSurface();
            configCodec(codec, format);
        } else {
            mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback2() {
                @Override
                public void surfaceRedrawNeeded(SurfaceHolder holder) {

                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    mSurface = holder.getSurface();
                    configCodec(codec, format);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            });

            return false;
        }
        return true;
    }

    @Override
    public void render(ByteBuffer buffer, MediaCodec.BufferInfo info) {

    }

    @Override
    public void doneDecode() {

    }

    @Override
    public void initRender() {

    }


}
