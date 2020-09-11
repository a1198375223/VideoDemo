package com.demo.videodemo.video;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.lifecycle.LifecycleOwner;


import com.demo.videodemo.utils.AppUtils;
import com.demo.videodemo.video.abs.AbstractPlayer;

import java.util.Map;

public class AndroidMediaPlayer extends AbstractPlayer {
    protected MediaPlayer mMediaPlayer;
    private boolean isLooping;
    protected Context mAppContext;
    private int mBufferedPercent;

    public AndroidMediaPlayer(LifecycleOwner owner) {
        super(owner);
        mAppContext = AppUtils.app().getApplicationContext();
    }


    @Override
    public void initPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(onErrorListener);
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        mMediaPlayer.setOnInfoListener(onInfoListener);
        mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
        mMediaPlayer.setOnPreparedListener(onPreparedListener);
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        try {
            mMediaPlayer.setDataSource(mAppContext, Uri.parse(path), headers);
        } catch (Exception e) {
            if (mPlayerEventListener != null) {
                mPlayerEventListener.onError();
            }
            mState = State.ERROR;
        }
    }

    @Override
    public void setDataSource(AssetFileDescriptor fd) {
        try {
            mMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
        } catch (Exception e) {
            if (mPlayerEventListener != null) {
                mPlayerEventListener.onError();
            }
            mState = State.ERROR;
        }
    }

    @Override
    public void start() {
        try {
            mMediaPlayer.start();
            mState = State.PLAYING;
        } catch (IllegalStateException e) {
            if (mPlayerEventListener != null) {
                mPlayerEventListener.onError();
            }
            mState = State.ERROR;
        }
    }

    @Override
    public void pause() {
        try {
            mMediaPlayer.pause();
            mState = State.PAUSED;
        } catch (IllegalStateException e) {
            if (mPlayerEventListener != null) {
                mPlayerEventListener.onError();
            }
            mState = State.ERROR;
        }
    }

    @Override
    public void stop() {
        try {
            mMediaPlayer.stop();
            mState = State.IDLE;
        } catch (IllegalStateException e) {
            if (mPlayerEventListener != null) {
                mPlayerEventListener.onError();
            }
            mState = State.ERROR;
        }
    }

    @Override
    public void prepareAsync() {
        try {
            mMediaPlayer.prepareAsync();
            mState = State.PREPARING;
        } catch (IllegalStateException e) {
            if (mPlayerEventListener != null) {
                mPlayerEventListener.onError();
            }
            mState = State.ERROR;
        }
    }

    @Override
    public void reset() {
        mState = State.IDLE;
        mMediaPlayer.setVolume(1, 1);
        mMediaPlayer.reset();
        mMediaPlayer.setLooping(isLooping);
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public boolean isPaused() {
        return mState == State.PAUSED;
    }

    @Override
    public boolean isStopped() {
        return mState == State.IDLE;
    }

    @Override
    public void seekTo(long time) {
        try {
            mMediaPlayer.seekTo((int) time);
        } catch (IllegalStateException e) {
            if (mPlayerEventListener != null) {
                mPlayerEventListener.onError();
            }
            mState = State.ERROR;
        }
    }

    @Override
    public void release() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mMediaPlayer.release();
                    mState = AbstractPlayer.State.IDLE;
                } catch (Exception e) {
                    e.printStackTrace();
                    mState = AbstractPlayer.State.ERROR;
                }
            }
        }.start();
    }

    @Override
    public long getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public int getBufferedPercentage() {
        return mBufferedPercent;
    }

    @Override
    public void setSurface(Surface surface) {
        mMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
    }

    @Override
    public void setVolume(float v1, float v2) {
        mMediaPlayer.setVolume(v1, v2);
    }

    @Override
    public void setLooping(boolean isLooping) {
        this.isLooping = isLooping;
        mMediaPlayer.setLooping(isLooping);
    }

    @Override
    public void setEnableMediaCodec(boolean isEnable) {
        // no support
    }

    @Override
    public void setOptions() {
        // no support
    }

    @Override
    public void setSpeed(float speed) {
        // no support
    }

    @Override
    public long getTcpSpeed() {
        // no support
        return 0;
    }

    private MediaPlayer.OnErrorListener onErrorListener = (mp, what, extra) -> {
        if (mPlayerEventListener != null) {
            mPlayerEventListener.onError();
        }
        mState = State.ERROR;
        return true;
    };

    private MediaPlayer.OnCompletionListener onCompletionListener = mp -> {
        if (mPlayerEventListener != null) {
            mPlayerEventListener.onCompletion();
        }
        mState = State.COMPLETED;
    };

    private MediaPlayer.OnInfoListener onInfoListener = (mp, what, extra) -> {
        if (mPlayerEventListener != null) {
            mPlayerEventListener.onInfo(what, extra);
        }

        switch (what) {
            case AbstractPlayer.MEDIA_INFO_BUFFERING_START:
                mState = State.BUFFERING;
                break;
            case AbstractPlayer.MEDIA_INFO_BUFFERING_END:
                mState = State.BUFFERED;
                break;
            case AbstractPlayer.MEDIA_INFO_VIDEO_RENDERING_START: // 视频开始渲染
                mState = State.PLAYING;
                break;
        }
        return true;
    };

    private MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mBufferedPercent = percent;
        }
    };


    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (mPlayerEventListener != null) {
                mPlayerEventListener.onPrepared();
            }
            mState = State.PREPARED;
            mMediaPlayer.start();
        }
    };

    private MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = (mp, width, height) -> {
        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        if (videoWidth != 0 && videoHeight != 0 && mPlayerEventListener != null) {
            mPlayerEventListener.onVideoSizeChanged(videoWidth, videoHeight);
        }
    };
}
