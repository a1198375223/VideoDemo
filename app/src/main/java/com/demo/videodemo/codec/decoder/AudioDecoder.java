package com.demo.videodemo.codec.decoder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;

import com.demo.videodemo.codec.BaseDecoder;
import com.demo.videodemo.codec.IExtractor;
import com.demo.videodemo.codec.extractor.AudioExtractor;

import java.nio.ByteBuffer;

public class AudioDecoder extends BaseDecoder {
    /**
     * 采样率
     */
    private int mSampleRate = -1;

    /**
     * 声音通道数量
     */
    private int mChannels = 1;

    /**
     * PCM采样位数
     */
    private int mPCMEncodeBit = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * 音频播放器
     */
    private AudioTrack mAudioTrack = null;

    /**
     * 音频数据缓存
     */
    private short[] mAudioOutTempBuf = null;

    public AudioDecoder(String filePath) {
        super(filePath);
    }

    @Override
    public IExtractor createExtractor(String path) {
        return new AudioExtractor(path);
    }

    @Override
    public void initParams(MediaFormat format) {
        mChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);

        if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
            mPCMEncodeBit = format.getInteger(MediaFormat.KEY_PCM_ENCODING);
        } else {
            //如果没有这个参数，默认为16位采样
            mPCMEncodeBit = AudioFormat.ENCODING_PCM_16BIT;
        }
    }

    @Override
    public boolean configCodec(MediaCodec codec, MediaFormat format) {
        codec.configure(format, null, null, 0);
        return true;
    }

    @Override
    public void render(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        if (mAudioOutTempBuf.length < info.size / 2) {
            mAudioOutTempBuf = new short[info.size / 2];
        }
        buffer.position(0);
        buffer.asShortBuffer().get(mAudioOutTempBuf, 0, info.size / 2);
        mAudioTrack.write(mAudioOutTempBuf, 0, info.size / 2);
    }

    @Override
    public void doneDecode() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
        }
    }

    @Override
    public void initRender() {
        int channel;
        if (mChannels == 1) {
            //单声道
            channel = AudioFormat.CHANNEL_OUT_MONO;
        } else {
            //双声道
            channel = AudioFormat.CHANNEL_OUT_STEREO;
        }

        //获取最小缓冲区
        int minBufferSize = AudioTrack.getMinBufferSize(mSampleRate, channel, mPCMEncodeBit);

        mAudioOutTempBuf = new short[minBufferSize / 2];

        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,//播放类型：音乐
                mSampleRate, //采样率
                channel, //通道
                mPCMEncodeBit, //采样位数
                minBufferSize, //缓冲区大小
                AudioTrack.MODE_STREAM); //播放模式：数据流动态写入，另一种是一次性写入

        mAudioTrack.play();
    }
}
