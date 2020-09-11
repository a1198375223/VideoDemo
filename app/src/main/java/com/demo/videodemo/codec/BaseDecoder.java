package com.demo.videodemo.codec;

import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class BaseDecoder implements IDecoder {
    protected final String TAG = IDecoder.class.getSimpleName();

    private DecodeState mState = DecodeState.STOP;
    private boolean mIsRunning = true;
    private String mFilePath;

    private MediaCodec mMediaCodec;
    private IExtractor mExtractor;
    private MediaCodec.BufferInfo mBufferInfo;


    private boolean mIsEOS = false;
    private int mVideoWidth;
    private int mVideoHeight;
    private long mDuration;
    private long mStartPos = 0;
    private long mEndPos = 0;

    private IDecoderStateListener mStateListener;



    public BaseDecoder(String filePath) {
        this.mFilePath = filePath;
        mBufferInfo = new MediaCodec.BufferInfo();
    }

    public void setStateListener(IDecoderStateListener listener) {
        this.mStateListener = listener;
    }

    // 在这里做解码流程处理
    @Override
    public void run() {

        if (mState == DecodeState.STOP) {
            mState = DecodeState.START;
        }
        if (mStateListener != null) mStateListener.decoderPrepare(this);


        // 第一步初始化MediaCodec，并且调用start方法进入Flushed状态
        step1();

        // 第二步进入循环解码
        while (mIsRunning) {
            if (!mIsRunning ||
                    mState == DecodeState.STOP) {
                mIsRunning = false;
                break;
            }
            //如果数据没有解码完毕，将数据推入解码器解码
            int index;
            if (!mIsEOS) {
                index = mMediaCodec.dequeueInputBuffer(1000);
                if (index >= 0) {
                    // 获取数据
                    ByteBuffer buffer = mMediaCodec.getInputBuffer(index);
                    // 读取数据
                    int sampleSize = mExtractor.readBuffer(buffer);
                    Log.d(TAG, "read sample time=" + mExtractor.getCurrentTimestamp() + " flag=" + mExtractor.getSampleFlag());
                    if (sampleSize < 0) {
                        mMediaCodec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        mIsEOS = true;
                    } else {
                        mMediaCodec.queueInputBuffer(index, 0, sampleSize, mExtractor.getCurrentTimestamp(), 0);
                    }
                }
            }

            index = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 1000);
            if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.d(TAG, "decoder没有output了！");
            } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = mMediaCodec.getOutputFormat();
                Log.d(TAG, "decoder output format changed: " + newFormat);
            } else if (index < 0) {
                Log.d(TAG, "unexpected result from decoder.dequeueOutputBuffer");
            } else {
                // 第三步进行渲染
                ByteBuffer byteBuffer = mMediaCodec.getOutputBuffer(index);
                render(byteBuffer, mBufferInfo);
                // 第四步保存解码帧数据
                Frame frame = new Frame();
                frame.setBufferInfo(mBufferInfo);
                frame.setFrameData(byteBuffer);
                frame.image = mMediaCodec.getOutputImage(index);
                Log.d(TAG, "run: image == null ? " + mMediaCodec.getOutputImage(index)) ;
//                Log.d(TAG, "run: buffer == null ? " + mMediaCodec.getOutputBuffer(index)) ;
                if (mStateListener != null) mStateListener.decodeOneFrame(this, frame);

                // 第五步释放缓冲
                mMediaCodec.releaseOutputBuffer(index, true);
            }
        }
        // 第六步判断是否解码成功
        if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
            Log.w(TAG, "解码结束");
            if (mStateListener != null) mStateListener.decoderFinish(this);
        }
        // 最后释放资源
        doneDecode();
        release();
    }

    // 在这里初始化解码器
    private boolean step1() {
        if (mFilePath.isEmpty() || !new File(mFilePath).exists()) {
            Log.w(TAG, "文件路径为空");
            if (mStateListener != null) mStateListener.decoderError(this, "文件路径为空");
            return false;
        }
        // 1. 初始化数据提取器
        mExtractor = createExtractor(mFilePath);

        if (mStateListener != null) mStateListener.decoderReady(this);

        // 数据提取器必须拥有一个format对象
        if (mExtractor == null || mExtractor.getFormat() == null) return false;

        // 使用format初始化一些参数
        MediaFormat format = mExtractor.getFormat();
        // 获取视频时长
        mDuration = format.getLong(MediaFormat.KEY_DURATION) / 1000;
        if (mEndPos == 0) mEndPos = mDuration;
        // 传给子类进行初始化特有的参数
        initParams(format);

        // 初始化渲染器
        initRender();
        // ----------------------------------------------------------

        // 2. 使用createDecoderByType()方法来创建解码器
        try {
            // 获取mime类型
            String mime = format.getString(MediaFormat.KEY_MIME);
            mMediaCodec = MediaCodec.createDecoderByType(mime);
            // 配置Decoder
            if (!configCodec(mMediaCodec, format)) {
                //
            }
            // 启动解码器
            mMediaCodec.start();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void release() {
        mState = DecodeState.STOP;
        mIsEOS = false;
        if (mExtractor != null) mExtractor.stop();
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
        }
        if (mStateListener != null) mStateListener.decoderDestroy(this);
    }

    @Override
    public long seekTo(long milliseconds) {
        return 0;
    }

    @Override
    public long seekAndPlay(long milliseconds) {
        return 0;
    }

    @Override
    public int getWidth() {
        return mVideoWidth;
    }

    @Override
    public int getHeight() {
        return mVideoHeight;
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    @Override
    public long getCurrentTimeStamp() {
        return mBufferInfo.presentationTimeUs ;
    }

    @Override
    public int getRotationAngle() {
        return 0;
    }

    @Override
    public MediaFormat getMediaFormat() {
        return mExtractor != null ? mExtractor.getFormat() : null;
    }

    @Override
    public String getFilePath() {
        return mFilePath;
    }

    @Override
    public int getTrack() {
        return 0;
    }

    @Override
    public void pause() {
        mState = DecodeState.DECODING;
    }

    @Override
    public void resume() {
        mState = DecodeState.DECODING;
    }

    @Override
    public void stop() {
        mState = DecodeState.STOP;
        mIsRunning = false;
    }

    @Override
    public boolean isDecoding() {
        return mState == DecodeState.DECODING;
    }

    @Override
    public boolean isStop() {
        return mState == DecodeState.STOP;
    }

    @Override
    public boolean isSeeking() {
        return mState == DecodeState.SEEKING;
    }

    // 由子类来创建数据提取器
    public abstract IExtractor createExtractor(String path);

    public abstract void initParams(MediaFormat format);

    public abstract boolean configCodec(MediaCodec codec, MediaFormat format);

    public abstract void render(ByteBuffer buffer, MediaCodec.BufferInfo info);

    public abstract void doneDecode();

    public abstract void initRender();

}
