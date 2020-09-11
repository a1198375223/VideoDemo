package com.demo.videodemo.codec.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.demo.videodemo.codec.extractor.AudioExtractor;
import com.demo.videodemo.codec.extractor.VideoExtractor;
import com.demo.videodemo.utils.ThreadPool;

import java.nio.ByteBuffer;

public class MP4Repack {

    private final String TAG = "MP4Repack";

    private AudioExtractor mAExtractor;
    private VideoExtractor mVExtractor;
    private MMuxer mMuxer;

    public MP4Repack(String path) {
        this.mAExtractor = new AudioExtractor(path);
        this.mVExtractor = new VideoExtractor(path);
        this.mMuxer = new MMuxer();
    }

    public void start() {
        MediaFormat audioFormat = mAExtractor.getFormat();
        MediaFormat videoFormat = mVExtractor.getFormat();

        if (audioFormat != null) {
            mMuxer.addAudioTrack(audioFormat);
        } else {
            mMuxer.setNoAudio();
        }
        if (videoFormat != null) {
            mMuxer.addVideoTrack(videoFormat);
        } else {
            mMuxer.setNoVideo();
        }

        ThreadPool.runOnIOPool(() -> {
            ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            if (audioFormat != null) {
                int size = mAExtractor.readBuffer(buffer);
                while (size > 0) {
                    bufferInfo.set(0, size, mAExtractor.getCurrentTimestamp(), mAExtractor.getSampleFlag());
                    mMuxer.writeAudioData(buffer, bufferInfo);
                    size = mAExtractor.readBuffer(buffer);
                }
            }
            if (videoFormat != null) {
                int size = mVExtractor.readBuffer(buffer);
                while (size > 0) {
                    bufferInfo.set(0, size, mVExtractor.getCurrentTimestamp(), mVExtractor.getSampleFlag());
                    mMuxer.writeVideoData(buffer, bufferInfo);
                    size = mVExtractor.readBuffer(buffer);
                }
            }
            mAExtractor.stop();
            mVExtractor.stop();
            mMuxer.releaseAudioTrack();
            mMuxer.releaseVideoTrack();
            Log.i(TAG, "MP4 重打包完成");
        });
    }
}
