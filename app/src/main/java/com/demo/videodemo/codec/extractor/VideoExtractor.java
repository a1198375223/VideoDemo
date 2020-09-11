package com.demo.videodemo.codec.extractor;

import android.media.MediaFormat;

import com.demo.videodemo.codec.IExtractor;

import java.nio.ByteBuffer;

public class VideoExtractor implements IExtractor {
    private SimpleExtractor mExtractor;

    public VideoExtractor(String path) {
        this.mExtractor = new SimpleExtractor(path);
    }

    @Override
    public int readBuffer(ByteBuffer byteBuffer) {
        return mExtractor.readBuffer(byteBuffer);
    }

    @Override
    public MediaFormat getFormat() {
        return mExtractor.getVideoFormat();
    }

    @Override
    public void stop() {
        mExtractor.stop();
    }

    @Override
    public long getCurrentTimestamp() {
        return mExtractor.getCurrentTimestamp();
    }

    @Override
    public long seek(long pos) {
        return mExtractor.seek(pos);
    }

    @Override
    public void setStartPos(long pos) {
        mExtractor.setStartPos(pos);
    }

    @Override
    public int getSampleFlag() {
        return mExtractor.getSampleFlag();
    }
}
