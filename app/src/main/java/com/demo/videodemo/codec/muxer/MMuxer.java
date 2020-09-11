package com.demo.videodemo.codec.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MMuxer {

    private final String TAG = "MMuxer";

    private String mPath;

    private MediaMuxer mMediaMuxer = null;

    private int mVideoTrackIndex = -1;
    private int mAudioTrackIndex = -1;

    private boolean mIsAudioTrackAdd = false;
    private boolean mIsVideoTrackAdd = false;

    private boolean mIsAudioEnd = false;
    private boolean mIsVideoEnd = false;

    private boolean mIsStart = false;

    private IMuxerStateListener mStateListener = null;

    public MMuxer() {
        String fileName = "LVideo_Test" + /*SimpleDateFormat("yyyyMM_dd-HHmmss").format(Date()) +*/ ".mp4";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        mPath = filePath + fileName;
        try {
            mMediaMuxer = new MediaMuxer(mPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addVideoTrack(MediaFormat mediaFormat) {
        if (mIsVideoTrackAdd) return;
        if (mMediaMuxer != null) {
             try {
                 mVideoTrackIndex = mMediaMuxer.addTrack(mediaFormat);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            Log.i(TAG, "添加视频轨道");
            mIsVideoTrackAdd = true;
            startMuxer();
        }
    }

    public void addAudioTrack(MediaFormat mediaFormat) {
        if (mIsAudioTrackAdd) return;
        if (mMediaMuxer != null) {
            try {
                mAudioTrackIndex = mMediaMuxer.addTrack(mediaFormat);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            Log.i(TAG, "添加音频轨道");
            mIsAudioTrackAdd = true;
            startMuxer();
        }
    }

    public void setNoAudio() {
        if (mIsAudioTrackAdd) return;
        mIsAudioTrackAdd = true;
        mIsAudioEnd = true;
        startMuxer();
    }

    public void setNoVideo() {
        if (mIsVideoTrackAdd) return;
        mIsVideoTrackAdd = true;
        mIsVideoEnd = true;
        startMuxer();
    }

    public void writeVideoData(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        if (mIsStart && mMediaMuxer != null) {
            mMediaMuxer.writeSampleData(mVideoTrackIndex, byteBuffer, bufferInfo);
        }
    }

    public void writeAudioData(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        if (mIsStart && mMediaMuxer != null) {
            mMediaMuxer.writeSampleData(mAudioTrackIndex, byteBuffer, bufferInfo);
        }
    }

    private void startMuxer() {
        if (mIsAudioTrackAdd && mIsVideoTrackAdd & mMediaMuxer != null) {
            mMediaMuxer.start();
            mIsStart = true;
            if (mStateListener != null)
                mStateListener.onMuxerStart();
            Log.i(TAG, "启动封装器");
        }
    }

    public void releaseVideoTrack() {
        mIsVideoEnd = true;
        release();
    }

    public void releaseAudioTrack() {
        mIsAudioEnd = true;
        release();
    }

    private void release() {
        if (mIsAudioEnd && mIsVideoEnd) {
            mIsAudioTrackAdd = false;
            mIsVideoTrackAdd = false;
            try {
                if (mMediaMuxer != null) {
                    mMediaMuxer.stop();
                    mMediaMuxer.release();
                    mMediaMuxer = null;
                }
                Log.i(TAG, "退出封装器");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mStateListener != null)
                    mStateListener.onMuxerFinish();
            }
        }
    }

    public void setStateListener(IMuxerStateListener l) {
        this.mStateListener = l;
    }

    interface IMuxerStateListener {
        void onMuxerStart();

        void onMuxerFinish();
    }
}
