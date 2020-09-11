package com.demo.videodemo.video.abs;

import android.content.res.AssetFileDescriptor;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.IntDef;
import androidx.lifecycle.LifecycleOwner;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

public abstract class AbstractPlayer extends BasePresenter {

    public AbstractPlayer(LifecycleOwner owner) {
        super(owner);
    }

    @IntDef({State.ERROR, State.IDLE, State.PREPARING, State.PREPARED, State.PLAYING, State.PAUSED,
            State.COMPLETED, State.BUFFERING, State.BUFFERED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
        int ERROR = -1;
        int IDLE = 0;
        int PREPARING = 1;
        int PREPARED = 2;
        int PLAYING = 3;
        int PAUSED = 4;
        int COMPLETED = 5;
        int BUFFERING = 6;
        int BUFFERED = 7;
    }


    public static String convertStateToString(@State int state) {
        String stateString;
        switch (state) {
            case State.COMPLETED:
                stateString = "COMPLETED";
                break;
            case State.ERROR:
                stateString = "ERROR";
                break;
            case State.PAUSED:
                stateString = "PAUSED";
                break;
            case State.PLAYING:
                stateString = "PLAYING";
                break;
            case State.IDLE:
                stateString = "IDLE";
                break;
            case State.PREPARED:
                stateString = "PREPARED";
                break;
            case State.BUFFERED:
                stateString = "BUFFERED";
                break;
            case State.BUFFERING:
                stateString = "BUFFERING";
                break;
            case State.PREPARING:
                stateString = "PREPARING";
                break;
            default:
                stateString = "N/A";
        }
        return stateString;
    }

    /**
     * 开始渲染视频画面
     */
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;

    /**
     * 缓冲开始
     */
    public static final int MEDIA_INFO_BUFFERING_START = 701;

    /**
     * 缓冲结束
     */
    public static final int MEDIA_INFO_BUFFERING_END = 702;

    /**
     * 视频旋转信息
     */
    public static final int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;

    /**
     * 播放器事件回调
     */
    protected PlayerEventListener mPlayerEventListener;


    /**
     * 播放器的状态机
     */
    protected int mState = State.IDLE;

    /**
     * 初始化播放器实例
     */
    public abstract void initPlayer();

    /**
     * 设置播放地址
     * @param path 播放地址
     * @param headers 播放地址请求头
     */
    public abstract void setDataSource(String path, Map<String, String> headers);

    /**
     * 用于播放raw和asset里面的视频文件
     */
    public abstract void setDataSource(AssetFileDescriptor fd);

    /**
     * 播放
     */
    public abstract void start();

    /**
     * 暂停
     */
    public abstract void pause();

    /**
     * 停止
     */
    public abstract void stop();

    /**
     * 准备开始播放（异步）
     */
    public abstract void prepareAsync();

    /**
     * 重置播放器
     */
    public abstract void reset();

    /**
     * 是否正在播放
     */
    public abstract boolean isPlaying();

    /**
     * 是否处于暂停状态
     */
    public abstract boolean isPaused();

    /**
     * 是否处于停止状态
     */
    public abstract boolean isStopped();

    /**
     * 调整进度
     */
    public abstract void seekTo(long time);

    /**
     * 释放播放器
     */
    public abstract void release();

    /**
     * 获取当前播放的位置
     */
    public abstract long getCurrentPosition();

    /**
     * 获取视频总时长
     */
    public abstract long getDuration();

    /**
     * 获取缓冲百分比
     */
    public abstract int getBufferedPercentage();

    /**
     * 设置渲染视频的View,主要用于TextureView
     */
    public abstract void setSurface(Surface surface);

    /**
     * 设置渲染视频的View,主要用于SurfaceView
     */
    public abstract void setDisplay(SurfaceHolder holder);

    /**
     * 设置音量
     */
    public abstract void setVolume(float v1, float v2);

    /**
     * 设置是否循环播放
     */
    public abstract void setLooping(boolean isLooping);

    /**
     * 设置硬解码
     */
    public abstract void setEnableMediaCodec(boolean isEnable);

    /**
     * 设置其他播放配置
     */
    public abstract void setOptions();

    /**
     * 设置播放速度
     */
    public abstract void setSpeed(float speed);

    /**
     * 获取当前缓冲的网速
     */
    public abstract long getTcpSpeed();

    /**
     * 绑定VideoView
     */
    public void bindVideoView(PlayerEventListener playerEventListener) {
        this.mPlayerEventListener = playerEventListener;
    }
}
