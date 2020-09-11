package com.demo.videodemo.codec;

import android.media.MediaFormat;

import java.nio.ByteBuffer;

public interface IExtractor {

    // 读取数据
    int readBuffer(ByteBuffer byteBuffer);

    // 获取音视频格式参数
    MediaFormat getFormat();

    // 停止读取数据
    void stop();


    long getCurrentTimestamp();


    long seek(long pos);

    void setStartPos(long pos);

    int getSampleFlag();

}
