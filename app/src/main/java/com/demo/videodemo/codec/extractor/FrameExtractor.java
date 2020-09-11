package com.demo.videodemo.codec.extractor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.demo.videodemo.codec.view.VirtualSurfaceView;
import com.demo.videodemo.utils.ThreadPool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 封面提取器
 */
public class FrameExtractor {
    private static final String TAG = "FrameExtractor";
    // ---------- 图片相关 -------------
    private static final int COLOR_FormatI420 = 1;
    private static final int COLOR_FormatNV21 = 2;

    // ---------- 视频相关 -------------
    private MediaExtractor mExtractor;
    private MediaFormat mMediaFormat;
    private MediaCodec mMediaCodec;
    private MediaCodec.BufferInfo mBufferInfo;

    // ---------- 其他相关 -------------
    private VirtualSurfaceView mSurfaceView;
    private int mVideoTrack = -1;
    private String mFilePath;
    private long mDuration = -1;
    private int width;
    private int height;
    private boolean mIsEOS = false;
    private int fps;
    private int mFrameGapTime;
    private boolean mDoneInput = false;
    private long mDoneTime = -1;


    public FrameExtractor(String path) {
        mFilePath = path;
        mBufferInfo = new MediaCodec.BufferInfo();
        initMediaCodec();
    }


    // 在这里初始化解码器
    private void initMediaCodec() {
        if (mFilePath == null || mFilePath.isEmpty() || !new File(mFilePath).exists()) {
            Log.w(TAG, "文件路径为空");
            return;
        }
        this.mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(mFilePath);
            mMediaFormat = getVideoFormat();

            // 数据提取器必须拥有一个format对象
            if (mExtractor == null || mMediaFormat == null) return;
            // 获取视频时长
            mDuration = mMediaFormat.getLong(MediaFormat.KEY_DURATION);
            width = mMediaFormat.getInteger(MediaFormat.KEY_WIDTH);
            height = mMediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
            fps = mMediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
            mFrameGapTime = 1000 / fps * 1000; // us
            // 使用createDecoderByType()方法来创建解码器
            // 获取mime类型
            String mime = mMediaFormat.getString(MediaFormat.KEY_MIME);
            mMediaCodec = MediaCodec.createDecoderByType(mime);
            //mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 0);
            mSurfaceView = new VirtualSurfaceView(width, height);
            mMediaCodec.configure(mMediaFormat, mSurfaceView.getSurface(), null, 0);
            // 启动解码器
            mMediaCodec.start();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }


    public Bitmap getFrameAtTime(long timeUs) {
        Log.d(TAG, "getFrameAtTime: duration=" + mDuration + " frame time=" + timeUs + " fps=" + fps + " frame gap=" + mFrameGapTime);
        if (mDuration < timeUs || timeUs < 0) return null;
        mDoneTime = -1;
        mDoneInput = false;
        Bitmap bitmap = null;
        seekClosest(timeUs);
        long currentTime = mExtractor.getSampleTime();
        Log.d(TAG, "Current time=" + currentTime);

        // 计算时间
//        List<Long> timeList = new ArrayList<>();
//        long startUs = currentTime;
//        for (; startUs <= timeUs; startUs += mFrameGapTime) {
//            timeList.add(startUs);
//        }
//        mDoneTime = timeList.get(timeList.size() - 1);

        while (!mIsEOS) {
            // 开始推流
            int index;
            if (!mDoneInput) {
                index = mMediaCodec.dequeueInputBuffer(0);
                if (index >= 0) {
                    // 获取数据
                    ByteBuffer buffer = mMediaCodec.getInputBuffer(index);
                    buffer.clear();
                    // 读取数据
                    int readSampleCount = mExtractor.readSampleData(buffer, 0);
                    Log.d(TAG, "read sample time=" + mExtractor.getSampleTime() + " flag=" + mExtractor.getSampleFlags());
                    if (readSampleCount < 0) {
                        // 读到了结尾 说明有错误
                        Log.e(TAG, "End OS!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        mMediaCodec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        mIsEOS = true;
                    } else {
//                        if (timeList.size() > 0) {
//                            timeList.remove(currentTime);
//                        }
//                        if (timeList.size() == 0) {
//                            mDoneInput = true;
//                        }
                        Log.d(TAG, "Current time=" + currentTime);
                        mMediaCodec.queueInputBuffer(index, 0, readSampleCount, currentTime, 0);
                        mExtractor.advance();
                        currentTime = mExtractor.getSampleTime();
                        Log.d(TAG, "Next time=" + currentTime);

//                        if (mDoneInput) {
//                            index = mMediaCodec.dequeueInputBuffer(0);
                            //mMediaCodec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//                        }
                    }
                }
            }


            index = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 0);
            if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.d(TAG, "decoder没有output了！");
            } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = mMediaCodec.getOutputFormat();
                Log.d(TAG, "decoder output format changed: " + newFormat);
            } else if (index < 0) {
                Log.d(TAG, "unexpected result from decoder.dequeueOutputBuffer");
            } else {
                // 获取解码buffer
//                Image image = mMediaCodec.getOutputImage(index);
//                Log.d(TAG, "getFrameAtTime: image == null ? " + image);
//                byte[] bytes = getDataFromImage(image, COLOR_FormatNV21);
//                YuvImage yuv = new YuvImage(bytes, ImageFormat.NV21, width, height, null);
//                try {
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    yuv.compressToJpeg(new Rect(0, 0, width, height), 80, stream);
//
//                    bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
//
//                    stream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    if (image != null)
//                        image.close();
//                    // 释放缓冲
//                    mMediaCodec.releaseOutputBuffer(index, false);
//                    mMediaCodec.flush();
//                }

                // 释放缓冲
                mMediaCodec.releaseOutputBuffer(index, mBufferInfo.size > 0);
                mSurfaceView.drawImage(true);
                Log.d(TAG, "presentationTime=" + mBufferInfo.presentationTimeUs + " doneTime=" + mDoneTime);
                if (!mDoneInput && mBufferInfo.presentationTimeUs == mDoneTime) {
                    mMediaCodec.flush();
                    return mSurfaceView.getFrame();
                }

            }
        }
        return bitmap;
    }


    private MediaFormat getVideoFormat() {
        for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            String mime = mExtractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("video/")) {
                mVideoTrack = i;
                break;
            }
        }
        if (mVideoTrack >= 0) {
            mExtractor.selectTrack(mVideoTrack);
        }
        return mVideoTrack >= 0 ? mExtractor.getTrackFormat(mVideoTrack) : null;
    }

    private long seekClosest(long timeUs) {
        mExtractor.seekTo(timeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        return mExtractor.getSampleTime();
    }


    /**
     * 停止读取数据
     */
    public void stop() {
        if (mExtractor != null) {
            mExtractor.release();
            mExtractor = null;
        }

        if (mMediaCodec != null) {
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }


    private byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        Log.v(TAG, "get data from " + planes.length + " planes");
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            Log.v(TAG, "pixelStride " + pixelStride);
            Log.v(TAG, "rowStride " + rowStride);
            Log.v(TAG, "width " + width);
            Log.v(TAG, "height " + height);
            Log.v(TAG, "buffer size " + buffer.remaining());
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
            Log.v(TAG, "Finished reading data from plane " + i);
        }
        return data;
    }

    private boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }
}
