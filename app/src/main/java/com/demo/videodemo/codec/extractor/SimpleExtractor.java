package com.demo.videodemo.codec.extractor;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SimpleExtractor {
    private static final String TAG = "SimpleExtractor";
    private MediaExtractor mExtractor;
    private int mVideoTrack = -1;
    private int mAudioTrack = -1;
    private long mCurSampleTime = 0L;
    private int mCurSampleFlag = 0;
    private long mStartPos = 0;

    public SimpleExtractor(String path) {
        this.mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public MediaFormat getVideoFormat() {
        for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            String mime = mExtractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("video/")) {
                mVideoTrack = i;
                break;
            }
        }
        return mVideoTrack >= 0 ? mExtractor.getTrackFormat(mVideoTrack) : null;
    }

    public MediaFormat getAudioFormat() {
        for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            String mime = mExtractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("audio/")) {
                mAudioTrack = i;
                break;
            }
        }
        return mAudioTrack >= 0 ? mExtractor.getTrackFormat(mAudioTrack) : null;
    }

    public int readBuffer(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        selectSourceTrack();
        int readSampleCount = mExtractor.readSampleData(byteBuffer, 0);
        if (readSampleCount < 0) {
            return -1;
        }
        //记录当前帧的时间戳
        mCurSampleTime = mExtractor.getSampleTime();
        mCurSampleFlag = mExtractor.getSampleFlags();
        //进入下一帧
        mExtractor.advance();
        return readSampleCount;
    }


    /**
     * 选择通道
     */
    private void selectSourceTrack() {
        if (mVideoTrack >= 0) {
            mExtractor.selectTrack(mVideoTrack);
        } else if (mAudioTrack >= 0) {
            mExtractor.selectTrack(mAudioTrack);
        }
    }


    public long seek(long pos) {
        mExtractor.seekTo(pos, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        return mExtractor.getSampleTime();
    }

    private long seekClosest(long pos) {
        mExtractor.seekTo(pos, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        return mExtractor.getSampleTime();
    }


    public ByteBuffer getFrameAtTime(long timeMs) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
        MediaFormat format = getVideoFormat();
        selectSourceTrack();
        long time = seekClosest(timeMs);
        Log.d(TAG, "getFrameAtTime: time=" + time);
        int readSampleCount = mExtractor.readSampleData(byteBuffer, 0);
        if (readSampleCount < 0) {
            return null;
        }
        return byteBuffer;
    }

    /**
     * 停止读取数据
     */
    public void stop() {
        mExtractor.release();
        mExtractor = null;
    }

    public int getVideoTrack() {
        return mVideoTrack;
    }

    public int getAudioTrack() {
        return mAudioTrack;
    }

    public void setStartPos(long pos) {
        mStartPos = pos;
    }

    /**
     * 获取当前帧时间
     */
    public long getCurrentTimestamp() {
        return mCurSampleTime;
    }

    public int getSampleFlag() {
        return mCurSampleFlag;
    }
}
