package com.demo.videodemo.video;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.demo.videodemo.video.abs.AbstractPlayer;
import com.demo.videodemo.video.abs.IRenderView;
import com.demo.videodemo.video.helper.MeasureHelper;

/**
 * SurfaceView专门提供了嵌入视图层级的绘制界面, 开发者可以控制该界面像Size等的形式, 能保证界面在屏幕上的正确位置.
 * 局限：
 * 1. 由于是独立的一层View, 更像是独立的一个Window, 不能加上动画
 * 2. 两个SurfaceView不能相互覆盖
 *
 * SurfaceView可以用过SurfaceHolder.addCallback()在子线程中更新UI
 *
 * 优点：
 * 1. 使用双缓冲机制，播放视频时画面更流畅
 * 2. 可以在一个独立的线程中进行绘制，不会影响主线程
 */
public class SurfaceRenderView extends SurfaceView implements IRenderView, SurfaceHolder.Callback {
    private static final String TAG = "SurfaceRenderView";
    private MeasureHelper mMeasureHelper;

    private AbstractPlayer mMediaPlayer;


    public SurfaceRenderView(Context context) {
        super(context);
    }

    public SurfaceRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SurfaceRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public SurfaceRenderView(Context context, @NonNull AbstractPlayer player) {
        super(context);
        mMediaPlayer = player;
        initView();
    }

    private void initView() {
        mMeasureHelper = new MeasureHelper();
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    @Override
    public void setVideoRotation(int degree) {
        mMeasureHelper.setVideoRotation(degree);
        setRotation(degree);
    }

    @Override
    public void setScaleType(int scaleType) {
        mMeasureHelper.setScreenScale(scaleType);
        requestLayout();
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public Bitmap doScreenShot() {
        return null;
    }

    @Override
    public void release() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int[] measuredSize = mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measuredSize[0], measuredSize[1]);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
