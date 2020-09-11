package com.demo.videodemo.video.config;

import com.demo.videodemo.video.VideoView;

import java.util.ArrayList;
import java.util.List;

public class VideoViewManager {
    private static final String TAG = "VideoViewManager";

    /**
     * 当前正在播放的VideoView
     */
    private List<VideoView> mVideoViews = new ArrayList<>();


    private boolean mPlayOnMobileNetwork;

    private static VideoViewManager sInstance;

    private static VideoViewConfig sConfig;

    private VideoViewManager() {
        mPlayOnMobileNetwork = getConfig().mPlayOnMobileNetwork;
    }

    public static VideoViewManager getInstance() {
        if (sInstance == null) {
            synchronized (VideoViewManager.class) {
                if (sInstance == null) {
                    sInstance = new VideoViewManager();
                }
            }
        }
        return sInstance;
    }


    public static VideoViewConfig getConfig() {
        setConfig(null);
        return sConfig;
    }

    public static void setConfig(VideoViewConfig config) {
        if (sConfig == null) {
            synchronized (VideoViewConfig.class) {
                if (sConfig == null) {
                    sConfig = config == null ? VideoViewConfig.newBuilder().build() : config;
                }
            }
        }
    }

    public boolean playOnMobileNetwork() {
        return mPlayOnMobileNetwork;
    }

    public void setPlayOnMobileNetwork(boolean playOnMobileNetwork) {
        mPlayOnMobileNetwork = playOnMobileNetwork;
    }

    public void addVideoView(VideoView videoView) {
        mVideoViews.add(videoView);
    }

    public void removeVideoView(VideoView videoView) {
        mVideoViews.remove(videoView);
    }

    public List<VideoView> getVideoViews() {
        return mVideoViews;
    }

    @Deprecated
    public void releaseVideoPlayer() {
        release();
    }

    public void pause() {
        for (int i = 0; i < mVideoViews.size(); i++) {
            VideoView vv = mVideoViews.get(i);
            if (vv != null) {
                vv.pause();
            }
        }
    }

    public void resume() {
        for (int i = 0; i < mVideoViews.size(); i++) {
            VideoView vv = mVideoViews.get(i);
            if (vv != null) {
                vv.resume();
            }
        }
    }

    public void release() {
        for (int i = 0; i < mVideoViews.size(); i++) {
            VideoView vv = mVideoViews.get(i);
            if (vv != null) {
                vv.release();
                i--;
            }
        }
    }

    public boolean onBackPressed() {
        for (int i = 0; i < mVideoViews.size(); i++) {
            VideoView vv = mVideoViews.get(i);
            if (vv != null) {
                boolean b = vv.onBackPressed();
                if (b) return true;
            }
        }
        return false;
    }
}
