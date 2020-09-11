package com.demo.videodemo.video.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.demo.videodemo.R;
import com.demo.videodemo.utils.PlayerUtils;
import com.demo.videodemo.utils.ThreadPool;
import com.demo.videodemo.video.StatusView;
import com.demo.videodemo.video.abs.AbstractPlayer;
import com.demo.videodemo.video.abs.MediaPlayerControl;
import com.demo.videodemo.video.config.VideoViewManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public abstract class BaseVideoController extends FrameLayout {
    private static final String TAG = "baseVideoController";

    protected View mControllerView;//控制器视图
    protected MediaPlayerControl mMediaPlayer;//播放器
    protected boolean mShowing;//控制器是否处于显示状态
    protected boolean mIsLocked;
    protected int mDefaultTimeout = 4000;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    @AbstractPlayer.State
    protected int mCurrentPlayState;
    protected StatusView mStatusView;

    public BaseVideoController(Context context) {
        this(context, null);
    }

    public BaseVideoController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseVideoController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    protected void init() {
        mControllerView = LayoutInflater.from(getContext()).inflate(getLayoutId(), this);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        mStatusView = new StatusView(getContext());
        setClickable(true);
        setFocusable(true);
    }

    /**
     * 设置控制器布局文件，子类必须实现
     */
    protected abstract int getLayoutId();

    /**
     * 显示
     */
    public void show() {}

    /**
     * 隐藏
     */
    public void hide() {}

    /**
     * 设置当前player的状态
     * @param playState 状态
     */
    public void setPlayState(int playState) {
        mCurrentPlayState = playState;
        hideStatusView();
        if (playState == AbstractPlayer.State.ERROR) {
            mStatusView.setMessage(getResources().getString(R.string.dkplayer_error_message));
            mStatusView.setButtonTextAndAction(getResources().getString(R.string.dkplayer_retry), v -> {
                hideStatusView();
                mMediaPlayer.replay(false);
            });
            this.addView(mStatusView, 0);
        }
    }

    /**
     * 显示状态view, 会在网络状态切换成移动网络的时候主动调用
     */
    public void showStatusView() {
        this.removeView(mStatusView);
        mStatusView.setMessage(getResources().getString(R.string.dkplayer_wifi_tip));
        mStatusView.setButtonTextAndAction(getResources().getString(R.string.dkplayer_continue_play), v -> {
            hideStatusView();
            VideoViewManager.getInstance().setPlayOnMobileNetwork(true);
            mMediaPlayer.start();
        });
        this.addView(mStatusView);
    }

    /**
     * 隐藏状态view
     */
    public void hideStatusView() {
        this.removeView(mStatusView);
    }

    /**
     * 设置播放器的状态
     * @param playerState 状态
     */
    public void setPlayerState(int playerState) {}

    /**
     * 切换暂停和播放状态
     */
    protected void doPauseResume() {
        if (mCurrentPlayState == AbstractPlayer.State.BUFFERING) return;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
    }

    /**
     * 横竖屏切换
     */
    protected void doStartStopFullScreen() {
        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity == null) return;
        if (mMediaPlayer.isFullScreen()) {
            mMediaPlayer.stopFullScreen();
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mMediaPlayer.startFullScreen();
        }
    }


    protected Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (mMediaPlayer.isPlaying()) {
                ThreadPool.runOnUiDelay(this, 1000 - (pos % 1000));
            }
        }
    };

    protected final Runnable mFadeOut = this::hide;

    protected int setProgress() {
        return 0;
    }

    /**
     * 获取当前系统时间
     */
    protected String getCurrentSystemTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date date = new Date();
        return simpleDateFormat.format(date);
    }

    protected String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ThreadPool.runOnUi(mShowProgress);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ThreadPool.removeFromUi(mShowProgress);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            ThreadPool.runOnUi(mShowProgress);
        }
    }

    /**
     * 改变返回键逻辑，用于activity
     */
    public boolean onBackPressed() {
        return false;
    }

    public void setMediaPlayer(MediaPlayerControl mediaPlayer) {
        this.mMediaPlayer = mediaPlayer;
    }
}

