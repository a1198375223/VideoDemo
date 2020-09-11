package com.demo.videodemo.codec;

import android.media.MediaFormat;

public interface IDecoder extends Runnable {

    // 暂停解码
    void pause();

    // 恢复解码
    void resume();

    // 停止解码
    void stop();

    // 是否正在解码
    boolean isDecoding();

    // 是否停止解码
    boolean isStop();

    boolean isSeeking();

    void setStateListener(IDecoderStateListener listener);


    long seekTo(long milliseconds);


    long seekAndPlay(long milliseconds);

    int getWidth();

    int getHeight();

    long getDuration();

    long getCurrentTimeStamp();

    int getRotationAngle();

    MediaFormat getMediaFormat();

    String getFilePath();

    int getTrack();
}
