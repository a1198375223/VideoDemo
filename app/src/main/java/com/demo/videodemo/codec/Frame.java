package com.demo.videodemo.codec;

import android.media.Image;
import android.media.MediaCodec;

import java.nio.ByteBuffer;

/**
 * 存储一帧的数据
 */
public class Frame {
    private ByteBuffer mByteBuffer;
    public Image image;

    private MediaCodec.BufferInfo mBufferInfo;

    public Frame() {
        this.mBufferInfo = new MediaCodec.BufferInfo();

    }

    public void setBufferInfo(MediaCodec.BufferInfo info) {
        mBufferInfo.set(info.offset, info.size, info.presentationTimeUs, info.flags);
    }

    public void setFrameData(ByteBuffer buffer) {
        // 先暂时这么写，如果要长时间持有需要copy一份ByteBuffer
        // todo
        this.mByteBuffer = buffer;
    }
}
