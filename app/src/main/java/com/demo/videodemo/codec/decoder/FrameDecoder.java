package com.demo.videodemo.codec.decoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.demo.videodemo.codec.BaseDecoder;
import com.demo.videodemo.codec.IExtractor;
import com.demo.videodemo.codec.extractor.VideoExtractor;

import java.nio.ByteBuffer;

public class FrameDecoder extends BaseDecoder {

    public FrameDecoder(String filePath) {
        super(filePath);
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
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        codec.configure(format, null, null, 0);
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


